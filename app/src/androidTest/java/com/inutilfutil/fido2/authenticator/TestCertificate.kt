package com.inutilfutil.fido2.authenticator

import com.google.common.base.Charsets
import com.google.common.io.BaseEncoding
import com.inutilfutil.fido2.authenticator.apdu.ApduRequest
import com.inutilfutil.fido2.authenticator.crypto.Certificate
import com.inutilfutil.fido2.authenticator.crypto.KeyGenerator
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.Assert
import org.junit.Test
import java.security.Security
import java.security.Signature
import java.security.interfaces.ECPrivateKey

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class TestCertificate {
    companion object {
        init {
            Security.removeProvider("BC");
            Security.addProvider(BouncyCastleProvider());
        }
    }


    @Test
    fun testCtap1Register() {
        val request = ApduRequest(cla=0x00.toUByte(), ins=CTAPAuthenticator.CTAP1_INS_REGISTRATION_REQUEST, p1=0.toUByte(), p2=0.toUByte(), data=UByteArray(64), le=UShort.MAX_VALUE)
        val response = CTAPAuthenticator().processCtap1(request)
        print(response)
    }

    @Test
    fun testSign() {
        val keyPair = KeyGenerator().generate()
        val msg = "msg".toByteArray(Charsets.UTF_8)
        val privateKeyBytes = (keyPair.private as ECPrivateKey).s.toByteArray()
        println("Private key: " + privateKeyBytes.size + " bytes = " + BaseEncoding.base16().encode(privateKeyBytes))

        val signature = Signature.getInstance("SHA256withECDSA")
        signature.initSign(keyPair.private)
        signature.update(msg)
        val signatureValue = signature.sign()
        println("Signature: " + signatureValue.size + " bytes = " + BaseEncoding.base16().encode(signatureValue))
        Assert.assertTrue(signatureValue.size in 70..73);

        signature.initVerify(keyPair.public);
        signature.update(msg);
        Assert.assertTrue(signature.verify(signatureValue));
    }
}