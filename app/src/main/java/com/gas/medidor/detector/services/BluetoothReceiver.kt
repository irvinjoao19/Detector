package com.gas.medidor.detector.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.bluetooth.BluetoothDevice
import android.os.Message


class BluetoothReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val msg = Message.obtain()
        val action = intent.action
        if (BluetoothDevice.ACTION_FOUND == action) {
            //Found, add to a device list
        }
    }
}
