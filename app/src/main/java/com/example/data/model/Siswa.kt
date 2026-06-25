package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "siswa")
data class Siswa(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nis: String,
    val namaLengkap: String,
    val kelompokUsia: String, // "KB-A" or "KB-B"
    val fotoUrl: String? = null
)
