package com.gas.medidor.detector

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.os.Build
import android.support.v7.app.AlertDialog
import android.support.v7.view.ContextThemeWrapper
import android.content.Intent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.support.annotation.RequiresApi
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import android.widget.*
import android.view.LayoutInflater
import java.util.*
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.util.Log
import java.io.*


class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onClick(v: View) {
        when (v.id) {
            R.id.buttonIniciar -> {
                if (isBluetoothEnabled()) {
                    listBluetooh()
                } else {
                    Toast.makeText(this, "Activar Bluetooth", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    lateinit var textViewMedidor: TextView
    lateinit var buttonIniciar: FloatingActionButton

    lateinit var builder: AlertDialog.Builder
    lateinit var dialog: AlertDialog

    private var btAdapter: BluetoothAdapter? = null
    private var btSocket: BluetoothSocket? = null
    private val recDataString = StringBuilder()
    private var mConnectedThread: ConnectedThread? = null
    private val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var bluetoothIn: Handler? = null
    private val handlerState = 0

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindToolbar()
        bindUI()

        bluetoothIn = @SuppressLint("HandlerLeak")
        object : Handler() {
            @SuppressLint("SetTextI18n")
            override fun handleMessage(msg: android.os.Message) {
                if (msg.what == handlerState) {
                    val readMessage = msg.obj as String
                    recDataString.append(readMessage)
                    val endOfLineIndex = recDataString.indexOf("~")
                    if (endOfLineIndex > 0) {
                        val dataInPrint = recDataString.substring(0, endOfLineIndex)
                        textViewMedidor.text = "Datos recibidos = $dataInPrint"
                    }
                    recDataString.delete(0, recDataString.length)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun bindToolbar() {
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        Objects.requireNonNull<ActionBar>(supportActionBar).title = "Medidor de Gas"
    }

    private fun bindUI() {
        textViewMedidor = findViewById(R.id.textViewMedidor)
        buttonIniciar = findViewById(R.id.buttonIniciar)
        buttonIniciar.setOnClickListener(this)
    }

    @SuppressLint("SetTextI18n")
    private fun listBluetooh() {
        if (btAdapter!!.isEnabled) {
            builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.AppTheme))
            @SuppressLint("InflateParams") val v = LayoutInflater.from(this@MainActivity).inflate(R.layout.dialog_list_bluetooh, null)

            val listViewBlueTooth: ListView = v.findViewById(R.id.listViewBlueTooth)
            val textViewTitulo: TextView = v.findViewById(R.id.textViewTitulo)
            textViewTitulo.text = "Bluetooth..."

            val listViewBlueToothAdapter = ArrayAdapter<String>(this, R.layout.device_name)
            btAdapter = BluetoothAdapter.getDefaultAdapter()

            val pairedDevices = btAdapter!!.bondedDevices
            if (pairedDevices.size > 0) {
                for (device in pairedDevices) {
                    listViewBlueToothAdapter.add(device.name + "\n" + device.address)
                }
            } else {
                val noDevices = "No hay dispositivos conectados"
                listViewBlueToothAdapter.add(noDevices)
            }

            listViewBlueTooth.adapter = listViewBlueToothAdapter
            listViewBlueTooth.onItemClickListener = AdapterView.OnItemClickListener { _, view, _, _ ->
                val info = (view as TextView).text.toString()
                val address = info.substring(info.length - 17)
                starBlue(address)
                dialog.dismiss()
            }

            builder.setView(v)
            dialog = builder.create()
            dialog.show()
        } else {
            Toast.makeText(this, "Activar Bluetooth", Toast.LENGTH_LONG).show()
        }
    }


    private fun errorExit(title: String, message: String) {
        Toast.makeText(this, "$title - $message", Toast.LENGTH_LONG).show()
        finish()
    }

    private inner class ConnectedThread
    internal constructor(socket: BluetoothSocket) : Thread() {
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?

        init {
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            try {
                tmpIn = socket.inputStream
                tmpOut = socket.outputStream
            } catch (ignored: IOException) {
            }

            mmInStream = tmpIn
            mmOutStream = tmpOut
        }

        override fun run() {
            val buffer = ByteArray(256)
            var bytes: Int
            while (true) {
                try {
                    bytes = mmInStream!!.read(buffer)
                    val readMessage = String(buffer, 0, bytes)
                    bluetoothIn?.obtainMessage(handlerState, bytes, -1, readMessage)?.sendToTarget()
                } catch (e: IOException) {
                    break
                }

            }
        }

        internal fun write(input: String) {
            val msgBuffer = input.toByteArray()
            try {
                mmOutStream!!.write(msgBuffer)
            } catch (e: IOException) {
                Toast.makeText(baseContext, "La Conexión fallo", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun starBlue(address: String) {

        val device = btAdapter?.getRemoteDevice(address)

        try {
            btSocket = createBluetoothSocket(device!!)
        } catch (e: IOException) {
            errorExit("Mensaje", "Socket error : " + e.message + ".")
        }

        btAdapter?.cancelDiscovery()
        try {
            btSocket?.connect()
        } catch (e: IOException) {
            try {
                btSocket?.close()
                Toast.makeText(this@MainActivity, "La Conexión fallo", Toast.LENGTH_LONG).show()
            } catch (e2: IOException) {
                errorExit("Mensaje", "Socket error : " + e.message + ".")
            }
        }

        mConnectedThread = ConnectedThread(btSocket!!)
        mConnectedThread?.start()
    }

    @Throws(IOException::class)
    private fun createBluetoothSocket(device: BluetoothDevice): BluetoothSocket {
        try {
            val m = device.javaClass.getMethod("createInsecureRfcommSocketToServiceRecord", UUID::class.java)
            return m.invoke(device, MY_UUID) as BluetoothSocket
        } catch (e: Exception) {
            Log.e("TAG", "Could not create Insecure RFComm Connection", e)
        }
        return device.createInsecureRfcommSocketToServiceRecord(MY_UUID)
    }


    private fun setBluetoothAdapter() {
        btAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    private fun isBluetoothEnabled(): Boolean {
        setBluetoothAdapter()
        return if (btAdapter != null) {
            if (!btAdapter!!.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, 1)
                btAdapter!!.isEnabled
            } else
                true
        } else
            false
    }

}


