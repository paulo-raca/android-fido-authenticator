package com.inutilfutil.fido2.authenticator.crypto

import org.bouncycastle.asn1.x9.ECNamedCurveTable
import org.bouncycastle.crypto.generators.ECKeyPairGenerator
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.crypto.params.ECKeyGenerationParameters
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.nio.ByteBuffer
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.util.*


class KeyHandle(val applicationHash: UByteArray, val encodedKeyHandle: UByteArray, val keyPair: KeyPair) {

    fun incrementAndGetCounter(): Int {
        return ++counter
    }

    companion object {
        var counter = 0
    }
}
