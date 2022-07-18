package com.inutilfutil.fido2.authenticator.comm

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.os.BuildCompat
import com.inutilfutil.fido2.authenticator.BuildConfig
import com.inutilfutil.fido2.authenticator.FidoApplication
import com.inutilfutil.fido2.authenticator.util.bleUuid
import java.util.*

class FidoBluetoothService : Service() {
    companion object {
        private val TAG = FidoBluetoothService::class.java.name
        val intent = Intent(FidoApplication.instance, FidoBluetoothService::class.java)
    }

    val bluetoothManager = FidoApplication.instance.getSystemService(BluetoothManager::class.java)
    val bluetoothAdapter = bluetoothManager.adapter
    var bluetoothLeAdvertiser: BluetoothLeAdvertiser? = null

    var gattServer : BluetoothGattServer? = null

    @SuppressLint("MissingPermission")
    val gattServerCallbacks = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            Log.i(TAG, "onConnectionStateChange, device=${device}, status=${status}, newState=${newState}")
        }

        override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
            super.onServiceAdded(status, service)
            Log.i(TAG, "onServiceAdded, service=${service}, status=${status}")
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic
        ) {
            Log.i(TAG, "onCharacteristicReadRequest: characteristic=${characteristic.uuid}, offset=${offset}")
            gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, "CharacteristicReadRequest".toByteArray(Charsets.UTF_8))
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            Log.i(TAG, "onCharacteristicWriteRequest: characteristic=${characteristic.uuid}, preparedWrite=${preparedWrite}, responseNeeded=${responseNeeded}, offset=${offset}, value=${Arrays.toString(value)}")
            gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
        }

        override fun onDescriptorReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            descriptor: BluetoothGattDescriptor?
        ) {
            Log.i(TAG, "onDescriptorReadRequest: device=${device}, offset=${offset}, descriptor=${descriptor}")
            gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
        }

        override fun onDescriptorWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            descriptor: BluetoothGattDescriptor?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            Log.i(TAG, "onDescriptorWriteRequest: device=${device}, offset=${offset}, descriptor=${descriptor}")
            gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
        }

        override fun onExecuteWrite(device: BluetoothDevice?, requestId: Int, execute: Boolean) {
            Log.i(TAG, "onExecuteWrite: device=${device}, execute=${execute}")
            super.onExecuteWrite(device, requestId, execute)
        }

        override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
            Log.i(TAG, "onNotificationSent: device=${device}")
            super.onNotificationSent(device, status)
        }

        override fun onPhyRead(device: BluetoothDevice?, txPhy: Int, rxPhy: Int, status: Int) {
            Log.i(TAG, "onPhyRead: device=${device}, txPhy=${txPhy}, rxPhy=${rxPhy}, status=${status}")
            super.onPhyRead(device, txPhy, rxPhy, status)
        }

        override fun onPhyUpdate(device: BluetoothDevice?, txPhy: Int, rxPhy: Int, status: Int) {
            Log.i(TAG, "onPhyUpdate: device=${device}, txPhy=${txPhy}, rxPhy=${rxPhy}, status=${status}")
            super.onPhyUpdate(device, txPhy, rxPhy, status)
        }

        override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
            Log.i(TAG, "onMtuChanged: device=${device}, mtu=${mtu}")
            super.onMtuChanged(device, mtu)
        }
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Log.i(TAG, "LE Advertise Started.")
        }

        override fun onStartFailure(errorCode: Int) {
            Log.w(TAG, "LE Advertise Failed: $errorCode")
        }
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)
            when (state) {
                BluetoothAdapter.STATE_ON -> {
                    startServer()
                }
                BluetoothAdapter.STATE_OFF -> {
                    stopServer()
                }
            }
        }
    }

    private fun startServer() {
        if (BuildCompat.isAtLeastS()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Missing BLUETOOTH_CONNECT permission")
                return
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Missing BLUETOOTH permission")
                return
            }
        }

        if (gattServer == null) {
            bluetoothManager.openGattServer(this, gattServerCallbacks).let {
                gattServer = it

                // FIXME: Android specifically blocks FIDO services:
                // https://android.googlesource.com/platform/packages/apps/Bluetooth/+/6f7f9bbf46acaaf266537256da4d0345909ea1c4/src/com/android/bluetooth/gatt/GattService.java#3217
                // To work around that, many implementations seem to be using HID-over-Bluetooth instead

                // TODO: I want to implement it anyway, so I'm building a patched AOSP image to test it

                Log.w(TAG, "Started GATT server")
            }
        }

        if (bluetoothLeAdvertiser == null) {
            bluetoothManager.adapter.bluetoothLeAdvertiser.let {
                bluetoothLeAdvertiser = it
                val settings = AdvertiseSettings.Builder()
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                    .setConnectable(true)
                    .setTimeout(0)
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                    .build()

                val data = AdvertiseData.Builder()
                    .setIncludeDeviceName(true)
                    .addServiceUuid(ParcelUuid(bleUuid(0xFFFD)))
                    .build()

                it.startAdvertising(settings, data, advertiseCallback)
                Log.i(TAG, "Bluetooth LE Advertising started")
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopServer() {
        gattServer.let {
            if (it != null) {
                it.close()
                gattServer = null
                Log.w(TAG, "Stopped GATT server")
            }
        }
        bluetoothLeAdvertiser.let {
            if (it != null) {
                it.stopAdvertising(advertiseCallback)
                Log.i(TAG, "Bluetooth LE Advertising stopped")
            }
        }
    }

    override fun onCreate() {
        if (bluetoothAdapter == null || !packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.w(TAG, "Device doesn't support Bluetooth LE")
            return
        }
        registerReceiver(bluetoothReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))


        if (bluetoothAdapter.isEnabled) {
            startServer()
        }

        Log.i(TAG, "Service Created")
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        unregisterReceiver(bluetoothReceiver)
        stopServer()
        Log.i(TAG, "Service Destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.i(TAG, "Service Bound")
        return null
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(TAG, "Service Unbound")
        return false
    }
}