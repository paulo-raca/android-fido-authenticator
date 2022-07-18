package com.inutilfutil.fido.authenticator.crypto

import java.security.KeyPair


class KeyHandle(val applicationHash: UByteArray, val encodedKeyHandle: UByteArray, val keyPair: KeyPair) {

    fun incrementAndGetCounter(): Int {
        return ++counter
    }

    companion object {
        var counter = 0
    }
}
