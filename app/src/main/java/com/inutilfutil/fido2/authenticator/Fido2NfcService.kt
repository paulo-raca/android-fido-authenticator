package com.inutilfutil.fido2.authenticator

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import com.inutilfutil.fido2.authenticator.apdu.ApduRequest
import com.inutilfutil.fido2.authenticator.apdu.ApduResponse

@kotlin.ExperimentalUnsignedTypes
class Fido2NfcService : HostApduService() {
    companion object {
        private val TAG = Fido2NfcService::class.qualifiedName

        private val SW_SUCCESS: UShort = 0x9000.toUShort()

    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate")
    }

    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        val request = ApduRequest.parse(commandApdu)
        val response = ApduResponse(SW_SUCCESS, "U2F_V2")
        Log.i(TAG, "processCommandApdu: " + request + " -> " + response)

        return response.encode()
    }

    override fun onDeactivated(reason: Int) {
        Log.i(TAG, "onDeactivated: " + reason)
    }
}
