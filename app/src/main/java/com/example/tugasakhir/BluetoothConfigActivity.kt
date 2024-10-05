package com.example.tugasakhir

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



class BluetoothConfigActivity : ComponentActivity() {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val locationManager by lazy { getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    private val bluetoothLeScanner by lazy { bluetoothAdapter?.bluetoothLeScanner }
    private var devices by mutableStateOf<List<BluetoothDevice>>(emptyList())
    private var deviceName by mutableStateOf("")
    private var serviceUUID by mutableStateOf("")
    private var showDialog by mutableStateOf(false)


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
        setContent {
            TugasAkhirTheme {
                BluetoothConfigScreen(bluetoothAdapter, locationManager, bluetoothLeScanner)
            }
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
                        if (scanning) "Scanning Devices..." else
                            if (isBluetoothOn) "Start Scanning" else "Turn On Bluetooth"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                bluetoothLeScanner?.startScan(scanCallback)
                delay(10000) // Adjust scan duration if necessary
                bluetoothLeScanner?.stopScan(scanCallback)
            }
        }
    }

    private fun connectToDevice(context: Context, device: BluetoothDevice) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val bluetoothGatt = device.connectGatt(context, true, object : BluetoothGattCallback() {
                override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                    super.onConnectionStateChange(gatt, status, newState)
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.i("BluetoothConfig", "Connected to GATT server.")
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
                        gatt?.discoverServices()
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.i("BluetoothConfig", "Disconnected from GATT server.")
                        gatt?.close() // Ensure GATT is closed when disconnected
                    }
                }

                override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                    super.onServicesDiscovered(gatt, status)
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        gatt?.services?.firstOrNull()?.let { service ->
                            val uuid = service.uuid.toString()
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
            })
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
