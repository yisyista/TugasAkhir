package com.example.tugasakhir

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.TRANSPORT_LE
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tugasakhir.ui.theme.TugasAkhirTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.Int
import kotlin.OptIn
import kotlin.arrayOf
import kotlin.getValue
import kotlin.lazy
import kotlin.let
import java.util.Locale
private var isConnected by mutableStateOf(false) // Track connection status



class BluetoothConfigActivity : ComponentActivity() {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val locationManager by lazy { getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    private val bluetoothLeScanner by lazy { bluetoothAdapter?.bluetoothLeScanner }
    private var devices by mutableStateOf<List<BluetoothDevice>>(emptyList())
    private var deviceName by mutableStateOf("")
    private var serviceUUID by mutableStateOf("")
    private var showDialog by mutableStateOf(false)
    private var showDialogFail by mutableStateOf(false)
    private val TAG = "BluetoothConfigActivity" // Ganti dengan nama kelas Anda
    private var discoverServicesRunnable: Runnable? = null // Deklarasi variabel
    private val bleHandler = Handler() // Inisialisasi handler
    private var bluetoothGatt: BluetoothGatt? = null // Declare the BluetoothGatt variable here
    private val GATT_INTERNAL_ERROR = 129





    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val device = result.device
            if (!devices.contains(device)) {
                devices = devices + device
            }
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            super.onBatchScanResults(results)
            results.forEach { result ->
                val device = result.device
                if (!devices.contains(device)) {
                    devices = devices + device
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("BluetoothConfig", "Scan failed with error code: $errorCode")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions(this)
        setContent {
            TugasAkhirTheme {
                BluetoothConfigScreen(bluetoothAdapter, locationManager, bluetoothLeScanner)
            }
        }

    }

    private fun checkPermissions(context: Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                REQUEST_PERMISSION_CODE
            )
        }
    }




    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun BluetoothConfigScreen(
        bluetoothAdapter: BluetoothAdapter?,
        locationManager: LocationManager,
        bluetoothLeScanner: android.bluetooth.le.BluetoothLeScanner?
    ) {
        val context = LocalContext.current
        var isBluetoothOn by remember { mutableStateOf(bluetoothAdapter?.isEnabled == true) }
        var isLocationEnabled by remember { mutableStateOf(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) }
        var scanning by remember { mutableStateOf(false) }

        LaunchedEffect(scanning) {
            if (scanning) {
                scanForDevices(context, bluetoothLeScanner)
                scanning = false
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Bluetooth Configuration") })
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(onClick = {
                    when {
                        !isBluetoothOn -> {
                            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                            ActivityCompat.requestPermissions(
                                context as Activity,
                                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                                REQUEST_ENABLE_BT
                            )
                            if (ActivityCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                context.startActivity(enableBtIntent)
                            }
                        }
                        !isLocationEnabled -> {
                            val enableLocationIntent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            context.startActivity(enableLocationIntent)
                        }
                        else -> scanning = true
                    }
                }) {
                    Text(
                        if (scanning && !isConnected) "Scanning Devices..." else
                            if (isBluetoothOn && !isConnected) "Start Scanning"
                            else if (isConnected) "Connected"
                            else "Turn On Bluetooth"

                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!isConnected) {
                    LazyColumn {
                        items(devices) { device ->
                            ListItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        connectToDevice(context, device)
                                    },
                                headlineContent = { Text(device.name ?: "Unknown Device") },
                                trailingContent = {
                                    Button(onClick = { connectToDevice(context, device) }) {
                                        Text("Connect")
                                    }
                                }
                            )
                        }
                    }
                } else {
                    Text("Connected to $deviceName")
                }
            }
        }


        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Connected") },
                text = { Text("Connected to $deviceName with UUID = $serviceUUID") },
                confirmButton = {
                    Button(onClick = {
                        showDialog = false
                        // Navigate back to MainActivity
                        context.startActivity(Intent(context, MainActivity::class.java))
                        (context as Activity).finish()
                    }) {
                        Text("Ok")
                    }
                }
            )
        }
        if (showDialogFail) {
            AlertDialog(
                onDismissRequest = { showDialogFail = false },
                title = { Text("Connection Failed") },
                text = { Text("Failed to connect to $deviceName. Please try again.") },
                confirmButton = {
                    Button(onClick = { showDialogFail = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }

    private suspend fun scanForDevices(
        context: Context,
        bluetoothLeScanner: android.bluetooth.le.BluetoothLeScanner?
    ) {
        withContext(Dispatchers.IO) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // UUID yang ingin Anda filter
                val targetUUID =
                    UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b") // Ganti dengan UUID yang Anda inginkan

                // Membuat filter berdasarkan UUID yang ditargetkan
                val scanFilter = ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid(targetUUID))
                    .build()

                // Membuat pengaturan scan (ScanSettings)
                val scanSettings = ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build()

                withContext(Dispatchers.IO) {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.BLUETOOTH_SCAN
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // Memulai scan dengan filter dan pengaturan yang telah ditentukan
                        bluetoothLeScanner?.startScan(
                            listOf(scanFilter),
                            scanSettings,
                            scanCallback
                        )
                        delay(10000) // Menentukan durasi pemindaian jika diperlukan
                        bluetoothLeScanner?.stopScan(scanCallback)
                    }

                    /* bluetoothLeScanner?.startScan(scanCallback)
                delay(10000) // Adjust scan duration if necessary
                bluetoothLeScanner?.stopScan(scanCallback) */
                }
            }
        }
    }

    private fun connectToDevice(context: Context, device: BluetoothDevice) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val bluetoothGatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
                override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                    super.onConnectionStateChange(gatt, status, newState)
                    Log.i("BluetoothConfig", "Connection state changed: $newState, status: $status")
                    if (status == GATT_SUCCESS) {
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            //We successfully connected, proceed with service discovery
                            Log.i("BluetoothConfig", "Connected to GATT server.")
                            isConnected = true // Update connection status
                            if (ActivityCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                ) != PackageManager.PERMISSION_GRANTED
                            )

                            //gatt?.discoverServices()
                            {
                                val bondState = device.bondState
                                // Take action depending on the bond state
                                when (bondState) {
                                    BluetoothDevice.BOND_NONE, BluetoothDevice.BOND_BONDED -> {
                                        // Connected to device, now proceed to discover its services but delay a bit if needed
                                        val delayWhenBonded =
                                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) 1000 else 0
                                        val delay =
                                            if (bondState == BluetoothDevice.BOND_BONDED) delayWhenBonded else 0

                                        Log.d(TAG, "Bond State: $bondState")
                                        Log.d(TAG, "Attempting to post Runnable for service discovery")

                                        discoverServicesRunnable = Runnable {
                                            Log.d(
                                                TAG,
                                                String.format(
                                                    Locale.ENGLISH,
                                                    "discovering services of '%s' with delay of %d ms",
                                                    device.name,
                                                    delay
                                                )
                                            )
                                            // Memanggil discoverServices dengan pemeriksaan null
                                            val result = gatt?.discoverServices() ?: run {
                                                Log.e(TAG, "gatt is null, cannot discover services")
                                                return@Runnable // Menghentikan eksekusi Runnable
                                            }

                                            if (!result) {
                                                Log.e(TAG, "discoverServices failed to start")
                                            }
                                            discoverServicesRunnable = null
                                        }
                                        // Menggunakan let untuk memastikan discoverServicesRunnable tidak null
                                        discoverServicesRunnable?.let { runnable ->
                                            bleHandler.postDelayed(runnable, delay.toLong())
                                        }
                                    }

                                    BluetoothDevice.BOND_BONDING -> {
                                        // Bonding process in progress, let it complete
                                        Log.i(TAG, "waiting for bonding to complete")
                                    }
                                }
                            }


                            showConnectionSucceedDialog(context)
                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            //We successufully disconnected on our own request
                            Log.i("BluetoothConfig", "Disconnected from GATT server.")
                            gatt?.close()
                            isConnected = false // Update connection status
                            showConnectionFailedDialog(context)
                        } else {
                            //Connecting or diconnecting, ignore
                        }
                    } else {
                        gatt?.close()
                        showConnectionFailedDialog(context)
                    }
                }

                private fun disconnect() {
                    bluetoothGatt?.let { gatt ->
                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return
                        }
                        gatt.disconnect() // Step 1: Disconnect
                        // The callback onConnectionStateChange will be triggered, and we handle closing there
                    }
                }

                // Define function to show failure dialog
                private fun showConnectionFailedDialog(context: Context) {
                    showDialogFail = true // Show a Compose AlertDialog to inform the user
                }

                private fun showConnectionSucceedDialog(context: Context) {
                    showDialog = true // Show a Compose AlertDialog to inform the user
                }


                override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                    super.onServicesDiscovered(gatt, status)
                    Log.w("BluetoothConfig", "onServicesDiscovered received: $status")

                    // Check if the service discovery succeeded. If not disconnect
                    if (status == GATT_INTERNAL_ERROR) {
                        Log.e(TAG, "Service discovery failed");
                        disconnect();
                        return;
                    }

                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        gatt?.services?.firstOrNull()?.let { service ->
                            val uuid = service.uuid.toString()
                            Log.i("BluetoothConfig", "Service UUID discovered: ${service.uuid}")
                            // Update the state to show the dialog
                            if (ActivityCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return
                            }
                            deviceName = device.name ?: "Unknown Device"
                            serviceUUID = uuid
                            showDialog = true

                        }
                    } else {
                        Log.w("BluetoothConfig", "onServicesDiscovered received: $status")
                    }
                }


            }, TRANSPORT_LE)
            Log.i("BluetoothConfig", "Attempting to connect to device: ${device.address}")

        } else {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSION_CODE
            )
        }
    }

    companion object {
        private const val REQUEST_ENABLE_BT = 1
        private const val REQUEST_PERMISSION_CODE = 2
    }
}
