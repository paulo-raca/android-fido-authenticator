package com.inutilfutil.fido.authenticator.util

import android.util.Log
import java.util.*

fun bleUuid(value: Int) : UUID {
    var valueStr = value.toString(16)
    while (valueStr.length < 8) {
        valueStr = "0" + valueStr
    }

    val ret = UUID.fromString("${valueStr}-0000-1000-8000-00805F9B34FB")
    Log.i("bleUuid", ret.toString())
    return ret
}