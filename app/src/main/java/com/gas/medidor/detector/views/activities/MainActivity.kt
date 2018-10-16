package com.gas.medidor.detector.views.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.view.ContextThemeWrapper
import com.gas.medidor.detector.R
import com.gas.medidor.detector.helper.Permission
import android.content.Intent
import android.bluetooth.BluetoothAdapter
import android.util.Log
import android.widget.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        var cantidad = 0
        when (requestCode) {
            1 -> {
                for (valor: Int in grantResults) {
                    if (valor == PackageManager.PERMISSION_DENIED) {
                        cantidad += 1
                    }
                }
                if (cantidad >= 1) {
                    buttonIniciar.visibility = View.GONE
                    messagePermission()
                } else {
                    buttonIniciar.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.buttonIniciar -> {
                checkBTState()
            }
        }
    }

    lateinit var textViewMedidor: TextView
    lateinit var buttonIniciar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindUI()
        if (Build.VERSION.SDK_INT >= 23) {
            permision()
        }
    }

    private fun bindUI() {
        buttonIniciar = findViewById(R.id.buttonIniciar)
        textViewMedidor = findViewById(R.id.textViewMedidor)
        buttonIniciar.setOnClickListener(this)
    }

    private fun messagePermission() {
        val builder = AlertDialog.Builder(ContextThemeWrapper(this@MainActivity, R.style.AppTheme))
        val dialog: AlertDialog

        builder.setTitle("Permisos Denegados")
        builder.setMessage("Debes de aceptar los permisos para poder acceder al aplicativo.")
        builder.setPositiveButton("Aceptar") { dialogInterface, _ ->
            permision()
            dialogInterface.dismiss()
        }
        dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun permision() {
        if (!Permission.hasPermissions(this@MainActivity, *Permission.PERMISSIONS)) {
            ActivityCompat.requestPermissions(this@MainActivity, Permission.PERMISSIONS, Permission.PERMISSION_ALL)
        }
    }

    private fun checkBTState() {
        // Check device has Bluetooth and that it is turned on
        val mBtAdapter = BluetoothAdapter.getDefaultAdapter() // CHECK THIS OUT THAT IT WORKS!!!
        if (mBtAdapter == null) {
            Toast.makeText(this, "Device does not support Bluetooth", Toast.LENGTH_SHORT).show()
        } else {
            if (mBtAdapter.isEnabled) {
                Log.d("TAG", "...Bluetooth ON...")
            } else {
                //Prompt user to turn on Bluetooth
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, 1)

            }
        }
    }

}
