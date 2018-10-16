package com.gas.medidor.detector.views.activities

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AlertDialog
import android.support.v7.view.ContextThemeWrapper
import com.gas.medidor.detector.R
import com.gas.medidor.detector.helper.Permission
import android.content.Intent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.support.annotation.RequiresApi
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import android.widget.*
import android.view.LayoutInflater
import java.util.*
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.Handler
import android.util.Log
import java.io.*


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
                    messagePermission()
                }
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
//            R.id.buttonIniciar -> {
//                listBluetooh()
//            }
//            R.id.buttonSend -> {
//                mConnectedThread = ConnectedThread(btSocket!!)
//                mConnectedThread?.start()
//                val bytes = editTextEnvio.text.toString()
//                mConnectedThread?.write(bytes)
//            }
        }
    }


    lateinit var textViewMedidor: TextView
    lateinit var editTextEnvio: EditText
    lateinit var buttonIniciar: FloatingActionButton
    lateinit var buttonSend: Button

    var mBluetoothAdapter: BluetoothAdapter? = null

    lateinit var builder: AlertDialog.Builder
    lateinit var dialog: AlertDialog
    var MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    internal val RECIEVE_MESSAGE = 1
    lateinit var handlerBlue: Handler
    private val sb = StringBuilder()
    internal val handlerState = 0
    private var btSocket: BluetoothSocket? = null
    private val recDataString = StringBuilder()

    private var mConnectedThread: ConnectedThread? = null

    // SPP UUID service - this should work for most devices
    private val BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    // String for MAC address
    private var address: String? = null


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindToolbar()
        bindUI()
        if (Build.VERSION.SDK_INT >= 23) {
            permision()
        }

//        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
//        registerReceiver(mReceiver, filter)

        handlerBlue = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: android.os.Message) {
                when (msg.what) {
                    RECIEVE_MESSAGE -> {
                        val readBuf = msg.obj as ByteArray
                        val strIncom = String(readBuf, 0, msg.arg1)                 // create string from bytes array
                        sb.append(strIncom)                                                // append string
                        val endOfLineIndex = sb.indexOf("\r\n")                            // determine the end-of-line
                        if (endOfLineIndex > 0) {                                            // if end-of-line,
                            val sbprint = sb.substring(0, endOfLineIndex)               // extract string
                            sb.delete(0, sb.length)                                      // and clear
                            textViewMedidor.text = "Data from Arduino: $sbprint"            // update TextView

                        }
                    }
                }//Log.d(TAG, "...String:"+ sb.toString() +  "Byte:" + msg.arg1 + "...");
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun bindToolbar() {
//        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
//        setSupportActionBar(toolbar)
//        Objects.requireNonNull<ActionBar>(supportActionBar).title = "Medidor de Gas"
    }

    private fun bindUI() {

//        textViewMedidor = findViewById(R.id.textViewMedidor)
//        editTextEnvio = findViewById(R.id.editTextEnvio)
//
//        buttonIniciar = findViewById(R.id.buttonIniciar)
//        buttonSend = findViewById(R.id.buttonSend)
//        buttonIniciar.setOnClickListener(this)
//        buttonSend.setOnClickListener(this)
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
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter!!.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, Permission.BLUE_REQUEST)
            }

        } else {
            Toast.makeText(this, "Dispositivo no soporta Bluetooth", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun listBluetooh() {
        if (mBluetoothAdapter!!.isEnabled) {
            builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.AppTheme))
            @SuppressLint("InflateParams") val v = LayoutInflater.from(this@MainActivity).inflate(R.layout.dialog_list_bluetooh, null)

            val listViewBlueTooth: ListView = v.findViewById(R.id.listViewBlueTooth)
            val textViewTitulo: TextView = v.findViewById(R.id.textViewTitulo)
            textViewTitulo.text = "Bluetooth..."

            val listViewBlueToothAdapter = ArrayAdapter<String>(this, R.layout.device_name)
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

            val pairedDevices = mBluetoothAdapter!!.bondedDevices
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


    override fun onResume() {
        super.onResume()
        checkBTState()


    }

    public override fun onPause() {
        super.onPause()

        Log.d("TAG", "...In onPause()...")

        try {
            btSocket?.close()
        } catch (e2: IOException) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.message + ".")
        }

    }

    private fun errorExit(title: String, message: String) {
        Toast.makeText(this, "$title - $message", Toast.LENGTH_LONG).show()
        finish()
    }


    private inner class ConnectedThread internal constructor(socket: BluetoothSocket) : Thread() {
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?

        init {
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.inputStream
                tmpOut = socket.outputStream
            } catch (ignored: IOException) {
            }

            mmInStream = tmpIn
            mmOutStream = tmpOut
        }

        override fun run() {
            val buffer = ByteArray(256)  // buffer store for the stream
            var bytes: Int // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream!!.read(buffer)        // Get number of bytes and message in "buffer"
                    handlerBlue.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget()     // Send to message queue Handler
                } catch (e: IOException) {
                    break
                }

            }
        }

        /* Call this from the main activity to send data to the remote device */
        internal fun write(message: String) {
            Log.d("TAG", "...Data to send: $message...")
            val msgBuffer = message.toByteArray()
            try {
                mmOutStream!!.write(msgBuffer)
            } catch (e: IOException) {
                Log.d("TAG", "...Error data send: " + e.message + "...")
            }
        }
    }


    fun starBlue(address: String) {

        Log.d("TAG", "...onResume - try connect...")


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val device = mBluetoothAdapter?.getRemoteDevice(address)


        try {
            btSocket = createBluetoothSocket(device!!)
        } catch (e: IOException) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.message + ".")
        }


        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        mBluetoothAdapter?.cancelDiscovery()

        // Establish the connection.  This will block until it connects.
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


        // Create a data stream so we can talk to server.
        Log.d("TAG", "...Create Socket...")

        mConnectedThread = ConnectedThread(btSocket!!)
        mConnectedThread?.start()

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


    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND == action) {
                // Get the BluetoothDevice object from the Intent
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                // Add the name and address to an array adapter to show in a ListView
//                mArrayAdapter.add(device.name + "\n" + device.address)
            }
        }
    }

}


