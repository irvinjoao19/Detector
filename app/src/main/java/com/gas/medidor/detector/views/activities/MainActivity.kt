package com.gas.medidor.detector.views.activities

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.os.Build
import android.support.v7.app.AlertDialog
import android.support.v7.view.ContextThemeWrapper
import com.gas.medidor.detector.R
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
    //    lateinit var editTextEnvio: EditText
    //    lateinit var buttonSend: Button


    lateinit var buttonIniciar: FloatingActionButton
//    lateinit var progressBar: ProgressBar
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
                if (msg.what == handlerState) {                                        //if message is what we want
                    val readMessage = msg.obj as String                                                                // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage)                                    //keep appending to string until ~
                    val endOfLineIndex = recDataString.indexOf("~")                    // determine the end-of-line
                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
                        val dataInPrint = recDataString.substring(0, endOfLineIndex)    // extract string
                        textViewMedidor.text = "Datos recibidos = $dataInPrint"
                    }
                    recDataString.delete(0, recDataString.length)                    //clear all string data
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
//        progressBar = findViewById(R.id.progressBar)
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
            listViewBlueTooth.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
//                progressBar.visibility = View.VISIBLE
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


    private inner class ConnectedThread//creation of the connect thread
    internal constructor(socket: BluetoothSocket) : Thread() {
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?

        init {
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            try {
                //Create I/O streams for connection
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

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream!!.read(buffer)            //read bytes from input buffer
                    val readMessage = String(buffer, 0, bytes)
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn?.obtainMessage(handlerState, bytes, -1, readMessage)?.sendToTarget()
                } catch (e: IOException) {
                    break
                }

            }
        }

        //write method
        internal fun write(input: String) {
            val msgBuffer = input.toByteArray()           //converts entered String into bytes
            try {
                mmOutStream!!.write(msgBuffer)                //write bytes over BT connection via outstream
            } catch (e: IOException) {
                //if you cannot write, close the application
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
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.message + ".")
        }

        btAdapter?.cancelDiscovery()

        Log.d("TAG", "...Connecting...")
        try {
            btSocket?.connect()
            Log.d("TAG", "....Connection ok...")
        } catch (e: IOException) {
            try {
                btSocket?.close()
                Log.d("TAG", "....Socket cerrado..")
            } catch (e2: IOException) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.message + ".")
            }
        }

        Log.d("TAG", "...Create Socket...")

        mConnectedThread = ConnectedThread(btSocket!!)
        mConnectedThread?.start()

//        progressBar.visibility = View.GONE

    }

    @Throws(IOException::class)
    private fun createBluetoothSocket(device: BluetoothDevice): BluetoothSocket {
//        try {
//            val m = device.javaClass.getMethod("createInsecureRfcommSocketToServiceRecord", UUID::class.java)
//            return m.invoke(device, MY_UUID) as BluetoothSocket
//        } catch (e: Exception) {
//            Log.e("TAG", "Could not create Insecure RFComm Connection", e)
//        }
        return device.createInsecureRfcommSocketToServiceRecord(MY_UUID)
    }


    private fun setBluetoothAdapter() {
        btAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    private fun isBluetoothEnabled(): Boolean {
        setBluetoothAdapter()
        return if (btAdapter != null) {
            if (!btAdapter!!.isEnabled) {
                //INSTANTIATE A NEW ACTIVITY FROM SYSTEM
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, 1)
                btAdapter!!.isEnabled
            } else
                true
        } else
            false
    }


}


