package com.inutilfutil.fido2.authenticator.crypto

import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaX509v1CertificateBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.math.BigInteger
import java.security.KeyPair
import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*


class Certificate (
    val keyPair: KeyPair,
    val encoded: ByteArray
) {
    companion object {
        fun makeRandom(issuer: String, subject: String): Certificate {
            val serial = BigInteger(128, SecureRandom())
            val keyPair = KeyGenerator().generate()
            val now = LocalDateTime.now()
            val start = now.minusDays(1)
            val end = now.plusYears(100)
            val certHolder = JcaX509v1CertificateBuilder(
                X500Name("CN=${issuer}"),
                serial,
                Date.from(start.toInstant(ZoneOffset.UTC)),
                Date.from(end.toInstant(ZoneOffset.UTC)),
                X500Name("CN=${subject}"),
                keyPair.public
            ).build(JcaContentSignerBuilder("SHA256WITHECDSA").build(keyPair.private))

            return Certificate(keyPair, certHolder.encoded)
        }
    }
}
