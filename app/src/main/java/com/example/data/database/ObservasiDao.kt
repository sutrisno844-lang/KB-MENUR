package com.example.data.database

import androidx.room.*
import com.example.data.model.ObservasiHarian
import kotlinx.coroutines.flow.Flow

@Dao
interface ObservasiDao {
    @Query("SELECT * FROM observasi_harian ORDER BY tanggal DESC")
    fun getAllObservasi(): Flow<List<ObservasiHarian>>

    @Query("SELECT * FROM observasi_harian WHERE siswaId = :siswaId ORDER BY tanggal DESC")
    fun getObservasiForSiswa(siswaId: Int): Flow<List<ObservasiHarian>>

    @Query("SELECT * FROM observasi_harian WHERE siswaId = :siswaId AND tanggal = :tanggal LIMIT 1")
    suspend fun getObservasiForSiswaOnDate(siswaId: Int, tanggal: String): ObservasiHarian?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObservasi(observasi: ObservasiHarian): Long

    @Delete
    suspend fun deleteObservasi(observasi: ObservasiHarian)
}
