package com.inutilfutil.fido2.authenticator.crypto;

interface KeyManager {
    /**
     * Generate a new KeyHandle bound to the specific Application Hash
     */
    fun generateKeyHandle(applicationHash: UByteArray) : KeyHandle

    /**
     * Given the handle and application key, retrieves the KeyHandle object
     */
    fun decodeKeyHandle(applicationHash: UByteArray, encodedKeyHandle: UByteArray) : KeyHandle?
}
