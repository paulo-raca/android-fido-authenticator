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
        return "ApduResponse(status=%04x, data=%s)".format(status.toInt(), BaseEncoding.base16().encode(data ?: ByteArray(0)))
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

    companion object {
        /** Applet selection failed */
        val SW_APPLET_SELECT_FAILED = 0x6999.toUShort()

        /** Response bytes remaining */
        val SW_BYTES_REMAINING_00 = 0x6100.toUShort()

        /** CLA value not supported */
        val SW_CLA_NOT_SUPPORTED = 0x6E00.toUShort()
        
        /** Command chaining not supported */
        val SW_COMMAND_CHAINING_NOT_SUPPORTED = 0x6884.toUShort()
        
        /** Command not allowed */
        val SW_COMMAND_NOT_ALLOWED = 0x6986.toUShort()
         
        /** Conditions of use not satisfied */
        val SW_CONDITIONS_NOT_SATISFIED = 0x6985.toUShort()
        
        /** Correct Expected Length (Le) */
        val SW_CORRECT_LENGTH_00 = 0x6C00.toUShort()
        
        /** Data invalid */
        val SW_DATA_INVALID = 0x6984.toUShort()

        /** Not enough memory space in the file */
        val SW_FILE_FULL = 0x6A84.toUShort()

        /** File invalid */
        val SW_FILE_INVALID = 0x6983.toUShort()

        /** File not found */
        val SW_FILE_NOT_FOUND = 0x6A82.toUShort()

        /** Function not supported */
        val SW_FUNC_NOT_SUPPORTED = 0x6A81.toUShort()

        /** Incorrect parameters (P1,P2) */
        val SW_INCORRECT_P1P2 = 0x6A86.toUShort()

        /** INS value not supported */
        val SW_INS_NOT_SUPPORTED = 0x6D00.toUShort()

        /** Last command in chain expected */
        val SW_LAST_COMMAND_EXPECTED = 0x6883.toUShort()

        /** Card does not support the operation on the specified logical channel */
        val SW_LOGICAL_CHANNEL_NOT_SUPPORTED = 0x6881.toUShort()

        /** No Error */
        val SW_NO_ERROR = 0x9000.toUShort()

        /** Record not found */
        val SW_RECORD_NOT_FOUND = 0x6A83.toUShort()

        /** Card does not support secure messaging */
        val SW_SECURE_MESSAGING_NOT_SUPPORTED = 0x6882.toUShort()

        /** Security condition not satisfied */
        val SW_SECURITY_STATUS_NOT_SATISFIED = 0x6982.toUShort()

        /** No precise diagnosis */
        val SW_UNKNOWN = 0x6F00.toUShort()

        /** Warning, card state unchanged */
        val SW_WARNING_STATE_UNCHANGED = 0x6200.toUShort()

        /** Wrong data */
        val SW_WRONG_DATA = 0x6A80.toUShort()

        /** Wrong length */
        val SW_WRONG_LENGTH = 0x6700.toUShort()

        /** Incorrect parameters (P1,P2) */
        val SW_WRONG_P1P2 = 0x6B00.toUShort()
    }
}