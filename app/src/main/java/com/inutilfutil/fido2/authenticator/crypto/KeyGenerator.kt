package com.inutilfutil.fido2.authenticator.crypto

import android.util.Log
import com.google.common.io.BaseEncoding
import java.nio.ByteBuffer
import java.security.AlgorithmParameters
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECParameterSpec
import java.util.*


class KeyGenerator {
    val keyPairGenerator: KeyPairGenerator

    companion object {
        val TAG = KeyGenerator::class.java.name
    }

    init {
        val parameters = AlgorithmParameters.getInstance("EC");
        parameters.init(ECGenParameterSpec("prime256v1"));

        keyPairGenerator = KeyPairGenerator.getInstance("EC");
        keyPairGenerator.initialize(parameters.getParameterSpec(ECParameterSpec::class.java))
    }

    fun generate(): KeyPair {
        val keyPair = keyPairGenerator.generateKeyPair()
        val privateKey = keyPair.private as ECPrivateKey
        val publicKey = keyPair.public as ECPublicKey
        val s = privateKey.s.toByteArray()
        val x = publicKey.w.affineX.toByteArray()
        val y = publicKey.w.affineY.toByteArray()
        Log.d(TAG, "Private key: s=" + s.size + " bytes = " + BaseEncoding.base16().encode(s))
        Log.d(TAG, "Public key: x=" + x.size + " bytes = " + BaseEncoding.base16().encode(x))
        Log.d(TAG, "Public key: y=" + y.size + " bytes = " + BaseEncoding.base16().encode(y))
        publicKey.fidoBytes
        return keyPair
    }
}

val ECPublicKey.fidoBytes: ByteArray
    get() {
        val ecPoint = this.w

        var x = ecPoint.getAffineX().toByteArray()
        var y = ecPoint.getAffineY().toByteArray()
        x = x.copyOfRange(Math.max(0, x.size - 32), x.size)
        y = y.copyOfRange(Math.max(0, y.size - 32), y.size)

        val ret = ByteBuffer.allocate(1 + 32 + 32)
        ret.put(4)  // P-256
        ret.position(ret.position() + 32 - x.size)
        ret.put(x)
        ret.position(ret.position() + 32 - y.size)
        ret.put(y)
        return ret.array()
    }
