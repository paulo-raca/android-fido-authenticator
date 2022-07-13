package com.inutilfutil.fido2.authenticator.crypto;

import android.util.Log
import com.google.common.io.BaseEncoding
import java.security.SecureRandom

class RamKeyManager : KeyManager {
    private val keyGenerator = KeyGenerator()
    private val random = SecureRandom()
    private val database = HashMap<String, KeyHandle>()

    override fun generateKeyHandle(applicationHash: UByteArray) : KeyHandle {
        val keypair = keyGenerator.generate()
        val encodedKeyHandle = ByteArray(16)
        random.nextBytes(encodedKeyHandle)

        val keyHandle = KeyHandle(applicationHash, encodedKeyHandle.toUByteArray(), keypair)
        database[getDatabaseKey(applicationHash, encodedKeyHandle.toUByteArray())] = keyHandle
        return keyHandle
    }

    override fun decodeKeyHandle(applicationHash: UByteArray, encodedKeyHandle: UByteArray) : KeyHandle? {
        val key = getDatabaseKey(applicationHash, encodedKeyHandle)
        Log.i(TAG, "Looking for " + key + ", has " + database.keys)
        return database[key]
    }

    private fun getDatabaseKey(applicationHash: UByteArray, encodedKeyHandle: UByteArray): String {
        return BaseEncoding.base16().encode(applicationHash.toByteArray()) + "." + BaseEncoding.base16().encode(encodedKeyHandle.toByteArray())
    }

    companion object {
        val TAG = RamKeyManager::class.java.name
    }
}
