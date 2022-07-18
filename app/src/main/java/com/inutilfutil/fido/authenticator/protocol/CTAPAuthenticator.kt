package com.inutilfutil.fido.authenticator.protocol

import android.util.Log
import com.google.common.base.Charsets
import com.inutilfutil.fido.authenticator.transport.apdu.ApduRequest
import com.inutilfutil.fido.authenticator.transport.apdu.ApduResponse
import com.inutilfutil.fido.authenticator.crypto.*
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.security.Signature
import java.security.interfaces.ECPublicKey


class CTAPAuthenticator(
    private val keyManager: KeyManager = RamKeyManager(),
    private val certificate: Certificate = Certificate.makeRandom("Fido2 Issuer", "Fido2")
) {

    companion object {
        private val TAG = CTAPAuthenticator::class.qualifiedName

        val authenticatorMakeCredential = 0x01.toUByte()
        val authenticatorGetAssertion = 0x02.toUByte()
        val authenticatorGetNextAssertion = 0x03.toUByte()
        val authenticatorGetInfo = 0x04.toUByte()
        val authenticatorClientPIN = 0x06.toUByte()
        val authenticatorReset = 0x07.toUByte()
        val authenticatorBioEnrollment = 0x09.toUByte()
        val authenticatorCredentialManagement = 0x0A.toUByte()
        val authenticatorSelection = 0x0B.toUByte()
        val authenticatorLargeBlobs = 0x0C.toUByte()
        val authenticatorConfig = 0x0D.toUByte()

        val CTAP1_INS_REGISTRATION_REQUEST = 1.toUByte()
        val CTAP1_INS_AUTHENTICATION_REQUEST = 2.toUByte()
        val CTAP1_INS_GET_VERSION = 3.toUByte()

        val CTAP1_AUTHENTICATE_CHECK_ONLY = 7.toUByte()
        val CTAP1_AUTHENTICATE_ENFORCE_USER_AND_SIGN = 3.toUByte()
        val CTAP1_AUTHENTICATE_DONT_ENFORCE_USER_AND_SIGN = 8.toUByte()

        val U2F_V2 = "U2F_V2".toByteArray(Charsets.UTF_8)


        val __pk = KeyGenerator().generate()
    }

    fun processCtap1(request: ApduRequest): ApduResponse {
        assert(request.cla == 0.toUByte())
        return when (request.ins) {
            CTAP1_INS_REGISTRATION_REQUEST -> {
                processCtap1RegistrationRequest(request)
            }
            CTAP1_INS_AUTHENTICATION_REQUEST -> {
                processCtap1AuthenticationRequest(request)
            }
            CTAP1_INS_GET_VERSION -> {
                processCtap1GetVersion(request)
            }
            else -> {
                ApduResponse(ApduResponse.SW_INS_NOT_SUPPORTED)
            }
        }
    }

    private fun processCtap1RegistrationRequest(request: ApduRequest): ApduResponse {
        if (request.data.size != 64) {
            return ApduResponse(ApduResponse.SW_WRONG_LENGTH)
        }
        if (!isUserPresent()) {
            return ApduResponse(ApduResponse.SW_CONDITIONS_NOT_SATISFIED)
        }

        val challengeHash = request.data.copyOfRange(0, 32)
        val applicationHash = request.data.copyOfRange(32, 64)

        val keyHandle = keyManager.generateKeyHandle(applicationHash)
        val signature = Signature.getInstance("SHA256withECDSA")
        signature.initSign(certificate.keyPair.private)
        signature.update(0)  // A byte reserved for future use
        signature.update(applicationHash.toByteArray())
        signature.update(challengeHash.toByteArray())
        signature.update(keyHandle.encodedKeyHandle.toByteArray())
        signature.update((keyHandle.keyPair.public as ECPublicKey).fidoBytes)

        val responseStream = ByteArrayOutputStream();
        responseStream.write(5) // Reserved Byte
        responseStream.write((keyHandle.keyPair.public as ECPublicKey).fidoBytes)  // User public Key
        responseStream.write(keyHandle.encodedKeyHandle.size)  // Key Handle Size + value
        responseStream.write(keyHandle.encodedKeyHandle.toByteArray())
        responseStream.write(certificate.encoded)
        responseStream.write(signature.sign())

        return ApduResponse(ApduResponse.SW_NO_ERROR, responseStream.toByteArray())
    }

    private fun processCtap1AuthenticationRequest(request: ApduRequest): ApduResponse {
        if (request.data.size < 65) {
            return ApduResponse(ApduResponse.SW_WRONG_LENGTH)
        }
        val control = request.p1
        val challengeHash = request.data.copyOfRange(0, 32)
        val applicationHash = request.data.copyOfRange(32, 64)
        val keyLength = request.data[64].toInt()
        if (request.data.size != 65 + keyLength) {
            return ApduResponse(ApduResponse.SW_WRONG_LENGTH)
        }
        val encodedKeyHandle = request.data.copyOfRange(65, 65+keyLength)
        val keyHandle = keyManager.decodeKeyHandle(applicationHash, encodedKeyHandle)

        // Check for invalid control command
        if (control != CTAP1_AUTHENTICATE_CHECK_ONLY && control != CTAP1_AUTHENTICATE_ENFORCE_USER_AND_SIGN && control != CTAP1_AUTHENTICATE_DONT_ENFORCE_USER_AND_SIGN) {
            return ApduResponse(ApduResponse.SW_WRONG_P1P2)
        }

        // Check for invalid key handle (for this authenticator/application)
        if (keyHandle == null) {
            return ApduResponse(ApduResponse.SW_WRONG_DATA)
        }

        // Check for user presence if necessary
        val isUserPresent = isUserPresent()
        if ((control == CTAP1_AUTHENTICATE_CHECK_ONLY) || (control == CTAP1_AUTHENTICATE_ENFORCE_USER_AND_SIGN && !isUserPresent)) {
            return ApduResponse(ApduResponse.SW_CONDITIONS_NOT_SATISFIED)
        }

        // Sign the request
        val counter = keyHandle.incrementAndGetCounter()
        val userPresenceByte = if (isUserPresent) 1.toByte() else 0.toByte()
        val counterBytes = ubyteArrayOf(
            counter.shl(24).toUByte(),
            counter.shl(16).toUByte(),
            counter.shl(8).toUByte(),
            counter.shl(0).toUByte())
        val signature = Signature.getInstance("SHA256withECDSA")
        signature.initSign(keyHandle.keyPair.private)
        signature.update(applicationHash.toByteArray())
        signature.update(userPresenceByte)
        signature.update(counterBytes.toByteArray())
        signature.update(challengeHash.toByteArray())

        val responseStream = ByteArrayOutputStream();
        responseStream.write(userPresenceByte.toInt())
        responseStream.write(counterBytes.toByteArray())
        responseStream.write(signature.sign())

        return ApduResponse(ApduResponse.SW_NO_ERROR, responseStream.toByteArray())
    }

    private fun processCtap1GetVersion(request: ApduRequest): ApduResponse {
        return ApduResponse(ApduResponse.SW_NO_ERROR, U2F_V2)
    }

    private fun processCtap2(command: UByte, data: InputStream): ApduResponse {
        Log.w(TAG, "processCtap2(cmd=${command}, data=${data}");
        when (command) {
            authenticatorGetInfo  -> {
                val response = HashMap<Int, Any>()
                response[1] = listOf("FIDO_2_0")
                response[3] = byteArrayOf(0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15)

                val out = ByteArrayOutputStream()
                out.write("Foobar".toByteArray())
                //val mapper = ObjectMapper(CBORFactory.builder().configure())
                //mapper.writeValue(out, response)

                Log.i(TAG, "authenticatorGetInfo response: ${response}")
                return ApduResponse(ApduResponse.SW_NO_ERROR, out.toByteArray());
            }
            else -> {
                return ApduResponse(ApduResponse.SW_UNKNOWN);
            }
        }
    }

    private fun isUserPresent(): Boolean {
        return true
    }
}