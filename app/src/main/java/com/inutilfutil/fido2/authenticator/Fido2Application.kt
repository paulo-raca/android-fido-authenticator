package com.inutilfutil.fido2.authenticator

import android.app.Application
import com.inutilfutil.fido2.authenticator.crypto.Certificate
import com.inutilfutil.fido2.authenticator.crypto.KeyManager
import com.inutilfutil.fido2.authenticator.crypto.RamKeyManager
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

class Fido2Application : Application() {
    companion object {
        init {
            /*Security.removeProvider("BC")
            // Confirm that positioning this provider at the end works for your needs!
            Security.addProvider(BouncyCastleProvider())*/
        }

        val keyManager: KeyManager = RamKeyManager()
        val certificate: Certificate = Certificate.makeRandom("Fido2 Issuer", "Fido2")
        val ctap: CTAPAuthenticator = CTAPAuthenticator(keyManager, certificate)
    }
}