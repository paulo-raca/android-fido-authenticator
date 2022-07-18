package com.inutilfutil.fido.authenticator.transport

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.inutilfutil.fido.authenticator.FidoApplication
import com.inutilfutil.fido.authenticator.R

class FidoForegroundService : Service() {
    companion object {
        private const val ONGOING_NOTIFICATION_ID = 1
        private val TAG = FidoForegroundService::class.java.name
        val intent = Intent(FidoApplication.instance, FidoForegroundService::class.java)
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    var bluetoothServiceConnection = object : ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.i(TAG, "Bound to Bluetooth Service")
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationManager = getSystemService(NotificationManager::class.java)
        val channelId = FidoForegroundService::class.java.name
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, getText(R.string.fg_channel_name), NotificationManager.IMPORTANCE_LOW)
            channel.importance = NotificationManager.IMPORTANCE_NONE
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getText(R.string.fg_service_title))
            .setContentText(getText(R.string.fg_service_msg))
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSmallIcon(R.drawable.ic_foreground_service)
            //.setContentIntent(pendingIntent)
            //.setTicker(getText(R.string.ticker_text))
            .build()

        startForeground(ONGOING_NOTIFICATION_ID, notification)
        Log.i(TAG, "Service started in foreground")


        bindService(FidoBluetoothService.intent, bluetoothServiceConnection, BIND_AUTO_CREATE)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        unbindService(bluetoothServiceConnection)

        Log.i(TAG, "Service destroyed")
    }
}