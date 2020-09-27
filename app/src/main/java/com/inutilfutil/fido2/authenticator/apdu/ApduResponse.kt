package com.inutilfutil.fido2.authenticator.apdu

import android.util.Xml
import com.google.common.io.BaseEncoding
import java.lang.IllegalArgumentException

data class ApduResponse(
    val status: UShort,
    val data: ByteArray?
) {

    constructor(status: UShort) : this(status, null)

    constructor(status: UShort, data: String) : this(status, data?.toByteArray(Charsets.US_ASCII))

    override fun toString(): String {
        return "ApduResponse(status=%04x, data=%s)".format(status.toInt(), BaseEncoding.base16().encode(data))
    }

    fun encode(): ByteArray {
        var ret: ByteArray
        if (data != null) {
            ret = ByteArray(data.size + 2)
            System.arraycopy(data, 0, ret, 0, data.size)
        } else {
            ret = ByteArray(2)
        }
        ret[ret.size - 2] = status.toInt().shr(8).toByte()
        ret[ret.size - 1] = status.toByte()
        return ret
    }
}