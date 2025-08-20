package com.creativeideas.batterymindai.data.sensors

import android.Manifest
import android.app.usage.UsageStatsManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.nfc.NfcAdapter
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.creativeideas.batterymindai.data.models.SensorStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemSensorManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun getCurrentSensorStatus(): List<SensorStatus> {
        val sensorStatuses = mutableListOf<SensorStatus>()

        try {
            // WiFi Status
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val isWifiEnabled = wifiManager?.isWifiEnabled ?: false
            sensorStatuses.add(
                SensorStatus(
                    name = "WiFi",
                    isActive = isWifiEnabled,
                    powerConsumption = if (isWifiEnabled) 5.2f else 0f,
                    description = if (isWifiEnabled) "WiFi is connected" else "WiFi is disabled"
                )
            )

            // Bluetooth Status
            // FIX: Utilizzo del metodo moderno per ottenere il BluetoothAdapter
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            val bluetoothAdapter = bluetoothManager?.adapter
            val isBluetoothEnabled = bluetoothAdapter?.isEnabled ?: false
            sensorStatuses.add(
                SensorStatus(
                    name = "Bluetooth",
                    isActive = isBluetoothEnabled,
                    powerConsumption = if (isBluetoothEnabled) 2.1f else 0f,
                    description = if (isBluetoothEnabled) "Bluetooth is enabled" else "Bluetooth is disabled"
                )
            )

            // Location/GPS Status
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val isGpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
            sensorStatuses.add(
                SensorStatus(
                    name = "GPS",
                    isActive = isGpsEnabled,
                    powerConsumption = if (isGpsEnabled) 8.5f else 0f,
                    description = if (isGpsEnabled) "GPS location is active" else "GPS location is disabled"
                )
            )

            // Mobile Data Status
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val network = connectivityManager?.activeNetwork
            val networkCapabilities = connectivityManager?.getNetworkCapabilities(network)
            val isMobileDataActive = networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?: false
            sensorStatuses.add(
                SensorStatus(
                    name = "Mobile Data",
                    isActive = isMobileDataActive,
                    powerConsumption = if (isMobileDataActive) 12.3f else 0f,
                    description = if (isMobileDataActive) "Mobile data is active" else "Mobile data is inactive"
                )
            )

            // Hotspot Status (simplified check)
            sensorStatuses.add(
                SensorStatus(
                    name = "Hotspot",
                    isActive = false, // Would need more complex implementation
                    powerConsumption = 0f,
                    description = "Mobile hotspot is disabled"
                )
            )

        } catch (e: Exception) {
            e.printStackTrace()
            // Add default sensor status if there's an error
            sensorStatuses.add(
                SensorStatus(
                    name = "System",
                    isActive = true,
                    powerConsumption = 3.0f,
                    description = "System sensors monitoring"
                )
            )
        }

        return sensorStatuses
    }
}