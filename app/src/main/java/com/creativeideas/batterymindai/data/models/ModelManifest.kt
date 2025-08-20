// C:\Users\Alessandro\AndroidStudioProjects\BatteryMind\app\src\main\java\com\creativeideas\batterymindai\data\models\ModelManifest.kt
package com.creativeideas.batterymindai.data.models

import kotlinx.serialization.Serializable

/**
 * Rappresenta il file di configurazione remoto che descrive l'ultima versione
 * del modello AI disponibile per il download.
 * Questo ci permette di aggiornare il modello senza rilasciare un nuovo APK.
 */
@Serializable
data class ModelManifest(
    val latestVersion: String,
    val url: String,
    val sizeInBytes: Long,
    val checksum: String, // SHA-256
    val checksumType: String = "SHA-256"
)