package com.inutilfutil.fido2.authenticator.comm

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import com.inutilfutil.fido2.authenticator.CTAPAuthenticator
import com.inutilfutil.fido2.authenticator.FidoApplication
import com.inutilfutil.fido2.authenticator.apdu.ApduRequest
import com.inutilfutil.fido2.authenticator.apdu.ApduResponse

@kotlin.ExperimentalUnsignedTypes
class FidoNfcService : HostApduService() {
    companion object {
        private val TAG = FidoNfcService::class.qualifiedName
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate")
    }

    fun processApdu(request: ApduRequest, extras: Bundle?): ApduResponse {
        Log.w(TAG, "processApdu(" + request + ")");

        // Applet selection, return version string
        if (request.cla.toInt() == 0x00 && request.ins.toInt() == 0xA4 && request.p1.toInt() == 0x04 && request.p2.toInt() == 0x00) {
            return ApduResponse(ApduResponse.SW_NO_ERROR, CTAPAuthenticator.U2F_V2);
        }

        // CTAP1 Command
        if (request.cla.toInt() == 0x0) {
            return FidoApplication.ctap.processCtap1(request)
        }

        // CTAP2 Command
        /*if (request.cla.toInt() == 0x80 && request.ins.toInt() == 0x10 && request.p1.toInt() == 0x00 && request.p2.toInt() == 0x00 && request.data != null && request.data.size >= 1) {
            val cmd = request.data[0].toUByte()
            return ido2Application.ctap.processCtap2(cmd, ByteArrayInputStream(request.data.toByteArray(), 1, request.data.size - 1))
        }*/

        Log.w(TAG, "Failed to process unknown command: $request")
        return ApduResponse(ApduResponse.SW_CLA_NOT_SUPPORTED);
    }

    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        var response: ApduResponse;
        try {
            val request = ApduRequest.parse(commandApdu.toUByteArray())
            response = processApdu(request, extras);

            if (response.data != null && response.data!!.size > request.le.toInt()) {
                //throw IOException("Response data has " + response.data!!.size + ", but maximum allowed is " + request.le);
            }

        } catch (t: Throwable) {
            Log.w(TAG, "processCommandApdu failed", t);
            response = ApduResponse(ApduResponse.SW_UNKNOWN);
        }

        Log.w(TAG, "response: $response");
        return response.encode()
    }

    override fun onDeactivated(reason: Int) {
        Log.i(TAG, "onDeactivated: " + reason)
    }
}
