package com.creativeideas.batterymindai.data.models

import kotlinx.serialization.Serializable

/**
 * Modella lo stato del modello di IA on-device in modo granulare e type-safe.
 * Questa è la "verità assoluta" sullo stato del nostro asset più critico.
 * È serializzabile per essere salvato facilmente nelle SharedPreferences.
 */
@Serializable
sealed class AIModelStatus {

    /**
     * Stato iniziale. Il modello non è presente sul dispositivo.
     */
    @Serializable
    data object NotDownloaded : AIModelStatus()

    /**
     * Il download è in corso. La UI può usare questo stato per mostrare un indicatore di progresso.
     */
    @Serializable
    data object Downloading : AIModelStatus()

    /**
     * Il download è fallito o la verifica dell'integrità non è andata a buon fine.
     * L'app deve tentare di nuovo o segnalare l'errore.
     */
    @Serializable
    data class DownloadFailed(val reason: String) : AIModelStatus()

    /**
     * Lo stato di successo. Il modello è stato scaricato, verificato ed è pronto per l'uso.
     * Contiene tutte le informazioni necessarie per caricarlo e validarlo.
     */
    @Serializable
    data class Ready(
        val version: String,      // Es. "1.0.1"
        val path: String,         // Percorso assoluto del file sul dispositivo
        val sizeInBytes: Long,
        val checksum: String      // Hash SHA-256 per la verifica dell'integrità
    ) : AIModelStatus()
}