package com.example.data.database

import androidx.room.*
import com.example.data.model.Siswa
import kotlinx.coroutines.flow.Flow

@Dao
interface SiswaDao {
    @Query("SELECT * FROM siswa ORDER BY namaLengkap ASC")
    fun getAllSiswa(): Flow<List<Siswa>>

    @Query("SELECT * FROM siswa WHERE kelompokUsia = :kelompok ORDER BY namaLengkap ASC")
    fun getSiswaByKelompok(kelompok: String): Flow<List<Siswa>>

    @Query("SELECT * FROM siswa WHERE id = :id")
    suspend fun getSiswaById(id: Int): Siswa?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSiswa(siswa: Siswa): Long

    @Delete
    suspend fun deleteSiswa(siswa: Siswa)
}
