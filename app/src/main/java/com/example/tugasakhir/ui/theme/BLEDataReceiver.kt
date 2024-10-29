package com.example.tugasakhir

import android.Manifest
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCallback
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import java.util.*
import android.content.Context

class BLEDataReceiver(private val context: Context) {

    fun setupDataReceiver(gatt: BluetoothGatt) {
        // Assuming the characteristic UUID for HRV data is known
        val hrvCharacteristicUUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8") // Replace with actual UUID
        val characteristic = gatt.getService(UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b"))?.getCharacteristic(hrvCharacteristicUUID)

        characteristic?.let {
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
            gatt.setCharacteristicNotification(it, true)
            // Configure descriptor for notifications (if needed)
            // val descriptor = it.getDescriptor(UUID.fromString("YOUR_DESCRIPTOR_UUID"))
            // descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            // gatt.writeDescriptor(descriptor)
        }

        Log.i("BLEDataReceiver", "Data receiver set up for HRV characteristic.")
    }

    fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        val data = characteristic.value // Get the byte array
        val hrvValue = parseHRVData(data) // Convert byte array to HRV value
        Log.i("BLEDataReceiver", "Received HRV data: $hrvValue")
    }

    private fun parseHRVData(data: ByteArray): String {
        // Convert the byte array to the desired format (e.g., String)
        return String(data) // Adjust according to how data is sent
    }
}
