package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "observasi_harian")
data class ObservasiHarian(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val siswaId: Int,
    val guruId: String,
    val tanggal: String, // "YYYY-MM-DD"
    val kegiatan: String,
    val catatanAnekdot: String,
    
    // Scale for each aspect (BB, MB, BSH, BSB or null)
    val nilaiAgama: String? = null,
    val nilaiMotorik: String? = null,
    val nilaiKognitif: String? = null,
    val nilaiBahasa: String? = null,
    val nilaiSosem: String? = null,
    val nilaiSeni: String? = null
)
