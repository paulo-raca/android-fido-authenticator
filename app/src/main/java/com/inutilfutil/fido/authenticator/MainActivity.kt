package com.inutilfutil.fido.authenticator

import android.app.Activity
import android.os.Bundle
import com.inutilfutil.fido.authenticator.transport.FidoForegroundService

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startService(FidoForegroundService.intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(FidoForegroundService.intent)
    }
}