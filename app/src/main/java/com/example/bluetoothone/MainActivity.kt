package com.example.bluetoothone

import android.Manifest.permission.BLUETOOTH_ADMIN
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    private var btPermission = false
    companion object {
        val extraAddress: String = "Device_address"
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnDevices = findViewById<Button>(R.id.btnSelectDevice)
        scanBluetooth(btnDevices)
        btnDevices.setOnClickListener { pairedDeviceList() }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun pairedDeviceList() {
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
        val pairedDevices: Set<BluetoothDevice>? = if (ActivityCompat.checkSelfPermission(
                this,
                BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                bluetoothPermissionLauncher.launch(BLUETOOTH_CONNECT)
            } else {
                bluetoothPermissionLauncher.launch(BLUETOOTH_ADMIN)
            }
            return
        } else {
            bluetoothAdapter!!.bondedDevices
        }
        val list: ArrayList<BluetoothDevice> = ArrayList()
        pairedDevices?.forEach { device ->
            val deviceName = device.name
            Log.i("Device", deviceName)
            list.add(device)
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        val listDevices = findViewById<ListView>(R.id.listyView)
        listDevices.adapter = adapter
        listDevices.setOnItemClickListener { _, _, position, _ ->
            val device: BluetoothDevice = list[position]
            val address: String = device.address

            val intent = Intent(this, ControlActivity::class.java)
            intent.putExtra(extraAddress, address)
            startActivity(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun scanBluetooth(view: View) {
        view.setOnClickListener {
            val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
            val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
            if (bluetoothAdapter == null) {
                Toast.makeText(this, "Device Doesn't Support Bluetooth", Toast.LENGTH_LONG).show()
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    bluetoothPermissionLauncher.launch(BLUETOOTH_CONNECT)
                } else {
                    bluetoothPermissionLauncher.launch(BLUETOOTH_ADMIN)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private val bluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
            val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
            btPermission = true
            if (bluetoothAdapter?.isEnabled == false) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                btActivityResultLauncher.launch(enableBtIntent)
            } else {
                btScan()
            }
        } else {
            btPermission = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private val btActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        when (result.resultCode) {
            RESULT_OK -> btScan()
            RESULT_CANCELED -> Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            else -> Toast.makeText(this, "Bluetooth Failed to Connect", Toast.LENGTH_LONG).show()
        }
    }

    private fun btScan() {
        Toast.makeText(this, "BlueTooth Connected Successfully", Toast.LENGTH_LONG).show()
        Log.d("BT", "Bluetooth Connected")
    }
}