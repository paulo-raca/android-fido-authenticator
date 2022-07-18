package com.inutilfutil.fido.authenticator

import android.app.Application
import com.inutilfutil.fido.authenticator.crypto.Certificate
import com.inutilfutil.fido.authenticator.crypto.KeyManager
import com.inutilfutil.fido.authenticator.crypto.RamKeyManager
import com.inutilfutil.fido.authenticator.protocol.CTAPAuthenticator

class FidoApplication : Application() {
    companion object {
        init {
            /*Security.removeProvider("BC")
            // Confirm that positioning this provider at the end works for your needs!
            Security.addProvider(BouncyCastleProvider())*/
        }

        val keyManager: KeyManager = RamKeyManager()
        val certificate: Certificate = Certificate.makeRandom("Fido2 Issuer", "Fido2")
        val ctap: CTAPAuthenticator = CTAPAuthenticator(keyManager, certificate)
        lateinit var instance: FidoApplication
    }

    init {
        FidoApplication.instance = this
    }
}