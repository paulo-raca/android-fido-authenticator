package com.inutilfutil.fido.authenticator.transport.apdu

import com.google.common.io.BaseEncoding

@kotlin.ExperimentalUnsignedTypes
data class ApduRequest(
    val cla: UByte,
    val ins: UByte,
    val p1: UByte,
    val p2: UByte,
    val data: UByteArray,
    val le: UShort
) {

    override fun toString(): String {
        var ret = "Apdu(CLA=%02x, INS=%02x, P1=%02x, P2=%02x".format(cla.toInt(), ins.toInt(), p1.toInt(), p2.toInt())
        if (data.isNotEmpty()) {
            ret += ", Lc=%d, data=%s".format(data.size, BaseEncoding.base16().encode(data.toByteArray()))
        } else {
            ret += ", Lc=0)"
        }
        ret += ", Le=%d)".format(le.toInt())
        return ret
    }

    companion object {
        fun parse(apdu: UByteArray): ApduRequest {
            try {
                var offset = 0
                val cla = apdu[offset++].toUByte()
                val ins = apdu[offset++].toUByte()
                val p1 = apdu[offset++].toUByte()
                val p2 = apdu[offset++].toUByte()

                var data: UByteArray
                var le: UShort

                // Body: [ Lc | DATA ] | [ Le ]
                var size = 0
                if (apdu.size > offset) {
                    size = apdu[offset++].toInt()
                    if (size == 0) {
                        size = (apdu[offset].toUInt().shl(8).or(apdu[offset + 1].toUInt())).toInt()
                        offset += 2
                    }
                }

                if (offset + size == apdu.size) {
                    // Whole buffer was consumed: No input data, the size actually refers to the max response size (Le)
                    data = UByteArray(0)
                    le = size.toUShort()
                } else {
                    // Copy input data, and maybe read the the max response size afterwards (Le)
                    data = apdu.copyOfRange(offset, offset+size)
                    offset += size.toInt()

                    if (offset == apdu.size) {
                        le = 0.toUShort()
                    } else if (offset + 1 == apdu.size) {
                        le = apdu[offset].toUShort()
                    } else if (offset + 2 == apdu.size) {
                        le = (apdu[offset].toInt().shl(8).or(apdu[offset + 1].toInt())).toUShort()
                        offset += 2
                    } else {
                        throw IllegalArgumentException("APDU buffer too big")
                    }
                }

                return ApduRequest(cla, ins, p1, p2, data, le)
            } catch (e: Exception) {
                throw IllegalArgumentException("Invalid APDU", e)
            }
        }
    }
}
