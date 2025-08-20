package com.creativeideas.batterymindai.data.sensors

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.creativeideas.batterymindai.data.models.AppUsageInfo
import com.creativeideas.batterymindai.data.preferences.AppPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUsageAnalyzer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPreferences: AppPreferences // Inietta le preferenze per sapere la modalità di accesso
) {

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    suspend fun getRealAppUsage(): List<AppUsageInfo> = withContext(Dispatchers.IO) {
        // Se l'utente ha scelto la modalità Root, prova prima il metodo più accurato.
        if (appPreferences.getAccessMode() == "Root") {
            val rootData = getAppUsageWithRoot()
            if (rootData.isNotEmpty()) {
                return@withContext rootData
            }
        }

        // Se la modalità Root fallisce o non è selezionata, usa il metodo standard come fallback.
        return@withContext getAppUsageWithoutRoot()
    }

    /**
     * Ottiene i dati di consumo della batteria direttamente dal servizio di sistema 'batterystats'
     * tramite un comando shell con privilegi di root. Questo è il metodo più accurato possibile.
     */
    private suspend fun getAppUsageWithRoot(): List<AppUsageInfo> {
        val packageManager = context.packageManager
        val appUsageMap = mutableMapOf<String, Double>()
        var totalPowerMah = 0.0

        try {
            val process = Runtime.getRuntime().exec("su -c dumpsys batterystats --checkin")
            val reader = BufferedReader(InputStreamReader(process.inputStream))

            reader.forEachLine { line ->
                val parts = line.split(",")
                if (parts.size > 5 && parts[1] == "uid" && parts[3] == "p") {
                    val uid = parts[4].substringAfter("_proc:").toIntOrNull()
                    val powerMah = parts[5].toDoubleOrNull()
                    if (uid != null && powerMah != null && powerMah > 0) {
                        val packages = packageManager.getPackagesForUid(uid)
                        packages?.forEach { packageName ->
                            appUsageMap[packageName] = (appUsageMap.getOrDefault(packageName, 0.0)) + powerMah
                        }
                    }
                }
            }

            totalPowerMah = appUsageMap.values.sum()
            Runtime.getRuntime().exec("su -c dumpsys batterystats --reset").waitFor()
            if (totalPowerMah == 0.0) totalPowerMah = 1.0

            return appUsageMap.mapNotNull { (packageName, powerMah) ->
                if (packageName == context.packageName) return@mapNotNull null

                try {
                    val appInfo = packageManager.getApplicationInfo(packageName, 0)
                    AppUsageInfo(
                        appName = packageManager.getApplicationLabel(appInfo).toString(),
                        packageName = packageName,
                        powerConsumption = powerMah.toFloat(),
                        usageTime = 0L,
                        batteryPercentage = ((powerMah / totalPowerMah) * 100).toFloat(),
                        isRunningInBackground = false,
                        isSystemApp = isSystemApp(appInfo)
                    )
                } catch (e: PackageManager.NameNotFoundException) {
                    null
                }
            }
                .filter { !it.isSystemApp || isCommonSystemApp(it.packageName) }
                .sortedByDescending { it.powerConsumption }
                .take(20)

        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    /**
     * Stima il consumo della batteria basandosi sul tempo di utilizzo in foreground.
     * Meno accurato del metodo root, ma non richiede permessi speciali.
     */
    private suspend fun getAppUsageWithoutRoot(): List<AppUsageInfo> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyList()

        try {
            val packageManager = context.packageManager
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val startTime = calendar.timeInMillis
            val endTime = System.currentTimeMillis()

            val usageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )

            val totalEstimatedPower = usageStats
                .filter { it.totalTimeInForeground > 0 }
                .sumOf { calculatePowerConsumption(it.totalTimeInForeground, it.packageName).toDouble() }
                .toFloat()
                .coerceAtLeast(1f)

            return usageStats.mapNotNull { usageStat ->
                if (usageStat.packageName == context.packageName) return@mapNotNull null

                try {
                    if (usageStat.totalTimeInForeground > 0) {
                        val appInfo = packageManager.getApplicationInfo(usageStat.packageName, 0)
                        val isSystem = isSystemApp(appInfo)

                        if (isSystem && !isCommonSystemApp(usageStat.packageName)) {
                            return@mapNotNull null
                        }

                        val powerConsumption = calculatePowerConsumption(usageStat.totalTimeInForeground, usageStat.packageName)

                        AppUsageInfo(
                            appName = packageManager.getApplicationLabel(appInfo).toString(),
                            packageName = usageStat.packageName,
                            powerConsumption = powerConsumption,
                            usageTime = usageStat.totalTimeInForeground,
                            isRunningInBackground = false,
                            batteryPercentage = (powerConsumption / totalEstimatedPower) * 100,
                            isSystemApp = isSystem
                        )
                    } else {
                        null
                    }
                } catch (e: PackageManager.NameNotFoundException) {
                    null
                }
            }.sortedByDescending { it.powerConsumption }.take(15)

        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    private fun calculatePowerConsumption(usageTimeMs: Long, packageName: String): Float {
        val usageHours = usageTimeMs / (1000f * 60f * 60f)
        val powerRateFactor = when {
            isGamingApp(packageName) -> 250f
            isVideoApp(packageName) -> 180f
            isSocialApp(packageName) -> 120f
            isBrowserApp(packageName) -> 100f
            isCommunicationApp(packageName) -> 80f
            else -> 60f
        }
        return usageHours * powerRateFactor
    }

    private fun isGamingApp(packageName: String): Boolean = packageName.contains("game") || packageName.contains("gaming")
    private fun isVideoApp(packageName: String): Boolean = listOf("youtube", "netflix", "twitch", "disney", "primevideo").any { packageName.contains(it) }
    private fun isSocialApp(packageName: String): Boolean = listOf("facebook", "instagram", "twitter", "tiktok", "snapchat", "reddit").any { packageName.contains(it) }
    private fun isBrowserApp(packageName: String): Boolean = listOf("chrome", "firefox", "opera", "duckduckgo", "samsung.android.internet").any { packageName.contains(it) }
    private fun isCommunicationApp(packageName: String): Boolean = listOf("whatsapp", "telegram", "messenger", "discord", "skype", "viber").any { packageName.contains(it) }
    private fun isSystemApp(appInfo: ApplicationInfo): Boolean = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
    private fun isCommonSystemApp(packageName: String): Boolean = listOf("com.android.chrome", "com.google.android.gm", "com.google.android.youtube", "com.android.vending").any { it == packageName }

    suspend fun hasUsageStatsPermission(): Boolean = withContext(Dispatchers.IO) {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return@withContext false
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, -5)
        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, calendar.timeInMillis, System.currentTimeMillis())
        return@withContext !stats.isNullOrEmpty()
    }
}