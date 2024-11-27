@file:Suppress("DEPRECATION")

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
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
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
import androidx.activity.viewModels
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Data

private var isConnected by mutableStateOf(false) // Track connection status

class BluetoothConfigActivity : ComponentActivity() {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val locationManager by lazy { getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    private val bluetoothLeScanner by lazy { bluetoothAdapter?.bluetoothLeScanner }
    private var devices by mutableStateOf<List<BluetoothDevice>>(emptyList())
    private var deviceName by mutableStateOf("")
    private var serviceUUID by mutableStateOf("")
    private var characteristicUUID by mutableStateOf("")
    private var showDialog by mutableStateOf(false)
    private var showDialogFail by mutableStateOf(false)
    private val TAG = "BluetoothConfigActivity" // Ganti dengan nama kelas Anda
    private var discoverServicesRunnable: Runnable? = null // Deklarasi variabel
    private val bleHandler = Handler() // Inisialisasi handler
    private var bluetoothGatt: BluetoothGatt? = null // Declare the BluetoothGatt variable here
    private val GATT_INTERNAL_ERROR = 129
    private val hrvViewModel: HrvViewModel by viewModels()
    val CLIENT_CHARACTERISTIC_CONFIG_UUID = "00002902-0000-1000-8000-00805f9b34fb"
    private val DEVICE_ADDRESS = "3C:71:BF:F1:4A:F6"


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
            // Mendapatkan instance BluetoothAdapter
            val bluetoothAdapter: BluetoothAdapter? = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

            // Mendapatkan BluetoothDevice dari alamat MAC
            val device: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice(DEVICE_ADDRESS)

            val bluetoothGatt = device?.connectGatt(context, false, object : BluetoothGattCallback() {
                override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                    super.onConnectionStateChange(gatt, status, newState)
                    Log.i("BluetoothConfig", "Connection state changed: $newState, status: $status")
                    if (status == GATT_SUCCESS) {
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            //We successfully connected, proceed with service discovery
                            Log.i("BluetoothConfig", "Connected to GATT server.")

                            bluetoothGatt = gatt
                            if (ActivityCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                ) != PackageManager.PERMISSION_GRANTED
                            )

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

                            //isConnected = true // Update connection status
                            Log.d("BluetoothConfig", "Before discovering services")
                            gatt?.discoverServices()


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
                        isConnected = false // Update connection status
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


                //@Deprecated("Deprecated in Java")
                override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                    super.onServicesDiscovered(gatt, status)
                    Log.w("BluetoothConfig", "onServicesDiscovered received: $status")

                    // Check if the service discovery succeeded. If not disconnect
                    if (status == GATT_INTERNAL_ERROR) {
                        Log.e(TAG, "Service discovery failed")
                        disconnect()
                        return
                    }

                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        //gatt?.services?.firstOrNull()?.let { service ->
                        val services = gatt?.services
                        for (service in services!!) {
                            //val uuid = service.uuid.toString()
                            // Asumsi Anda sudah mendapatkan service dari gatt
                            val serviceUUIDs = service.uuid.toString()
                            Log.i("BluetoothConfig", "Service UUID discovered: $serviceUUIDs") // Log UUID service
                            Log.d("BluetoothConfig", "Connected to device: ${gatt?.device?.address}")

                            // Mendapatkan daftar characteristics dari service
                            val characteristics = service.characteristics

                            // Cek jika characteristics tidak kosong
                            if (characteristics.isNotEmpty()) {
                                // Iterasi melalui semua characteristics untuk mendapatkan UUID
                                for (characteristic in characteristics) {
                                    val characteristicUUIDLocal = characteristic.uuid.toString()
                                    Log.d("BluetoothConfig", "Characteristic UUID: $characteristicUUIDLocal") // Log setiap characteristic UUID

                                    // Simpan atau gunakan characteristicUUID jika perlu
                                    // Misalnya, menyimpannya ke dalam variabel untuk digunakan nanti
                                    characteristicUUID = characteristicUUIDLocal // Pastikan Anda memiliki properti ini
                                }
                            } else {
                                Log.e("BluetoothConfig", "No characteristics found for service: $serviceUUID")

                            }

                            // Update the state to show the dialog
                            if (ActivityCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                return
                            }

                            deviceName = device.name ?: "Unknown Device"
                            serviceUUID = serviceUUIDs

                            val characteristic = getYourCharacteristic(gatt, serviceUUID, characteristicUUID) // Dapatkan characteristic yang diinginkan
                            Log.d("BluetoothConfig", "Characteristic UUID: ${characteristic.uuid}") // Debug: Log UUID characteristic

                            // Mengatur notifikasi untuk characteristic
                            val notificationSet = gatt.setCharacteristicNotification(characteristic, true)
                            Log.d("BluetoothConfig", "Notification set: $notificationSet") // Debug: Log status pengaturan notifikasi

                            // Dapatkan descriptor untuk mengatur notifikasi
                            val descriptor = characteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG_UUID))
                            if (descriptor != null) {
                                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                val descriptorWrite = gatt.writeDescriptor(descriptor)
                                Log.d("BluetoothConfig", "Descriptor write initiated: $descriptorWrite") // Debug: Log status penulisan descriptor
                            } else {
                                Log.e("BluetoothConfig", "Descriptor not found for characteristic: ${characteristic.uuid}") // Debug: Log jika descriptor tidak ditemukan
                            }

                            isConnected = true
                            showConnectionSucceedDialog(context)
                        }
                    } else {
                        Log.w("BluetoothConfig", "onServicesDiscovered received: $status")
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onCharacteristicChanged(
                    gatt: BluetoothGatt?,
                    characteristic: BluetoothGattCharacteristic?
                ) {
                    characteristic?.value?.let { data ->
                        Log.d("BLE", "Raw data: ${data.joinToString(", ")}")

                        // Pastikan panjang data sesuai (5 nilai: 2 int, 3 float = 5 * 4 bytes = 20 bytes)
                        if (data.size == 5 * 4) {
                            // Gunakan ByteBuffer untuk membaca data
                            val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)

                            // Ambil nilai dari byte array
                            val value1 = buffer.int
                            val value2 = buffer.float
                            val value3 = buffer.float
                            val value4 = buffer.int
                            val value5 = buffer.float

                            // Log nilai yang diterima
                            Log.d("BLE", "Received values: value1=$value1, value2=$value2, value3=$value3, value4=$value4, value5=$value5")

                            // Kirim data ke Worker untuk diproses
                            val workData = Data.Builder()
                                .putInt("NN20", value1)
                                .putFloat("SCR_Frequency", value2)
                                .putFloat("SCR_Amplitude_Max", value3)
                                .putInt("SCR_Number", value4)
                                .putFloat("SCR_Amplitude_STD", value5)
                                .build()

                            val workRequest = OneTimeWorkRequestBuilder<DataProcessingWorker>()
                                .setInputData(workData)
                                .build()

                            WorkManager.getInstance(context).enqueue(workRequest)

                            Log.d("BLE", "Data sent to Worker for processing")
                        } else {
                            Log.e("BLE", "Unexpected data size: ${data.size}, expected 20 bytes for 5 values")
                        }
                    }
                }



            }, TRANSPORT_LE)
            Log.i("BluetoothConfig", "Attempting to connect to device: ${device?.address}")

        } else {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSION_CODE
            )
        }
    }

    // Call this method whenever you receive new data from ESP32
    private fun getYourCharacteristic(gatt: BluetoothGatt, serviceUUID: String, characteristicUUID: String): BluetoothGattCharacteristic {
        //Mendapatkan UUID dari service yang diinginkan
        val characteristicUUID2 = UUID.fromString(characteristicUUID) // Ganti dengan UUID characteristic yang sebenarnya
        val serviceUUID2 = UUID.fromString(serviceUUID) // Pastikan serviceUUID valid


        // Debug: Log service UUID dan characteristic UUID
        Log.d("BluetoothConfig", "Service UUID: $serviceUUID2") // Log UUID service
        Log.d("BluetoothConfig", "Characteristic UUID: $characteristicUUID2") // Log UUID characteristic

        // Mendapatkan service dari gatt
        val service = gatt.getService(serviceUUID2)
        if (service == null) {
            Log.e("BluetoothConfig", "Service not found: $serviceUUID2") // Log jika service tidak ditemukan
            throw IllegalArgumentException("Service not found")
        }

        // Mendapatkan characteristic dari service
        val characteristic = service.getCharacteristic(characteristicUUID2)
        if (characteristic == null) {
            Log.e("BluetoothConfig", "Characteristic not found: $characteristicUUID2") // Log jika characteristic tidak ditemukan
            throw IllegalArgumentException("Characteristic not found")
        }

        // Debug: Log jika characteristic berhasil ditemukan
        Log.d("BluetoothConfig", "Characteristic found: $characteristicUUID2") // Log jika characteristic ditemukan

        return characteristic
    }


    companion object {
        private const val REQUEST_ENABLE_BT = 1
        private const val REQUEST_PERMISSION_CODE = 2
    }
}
