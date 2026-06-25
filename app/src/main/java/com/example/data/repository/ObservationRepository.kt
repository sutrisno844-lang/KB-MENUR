package com.example.data.repository

import android.util.Log
import com.example.data.database.SiswaDao
import com.example.data.database.ObservasiDao
import com.example.data.model.Siswa
import com.example.data.model.ObservasiHarian
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ObservationRepository(
    private val siswaDao: SiswaDao,
    private val observasiDao: ObservasiDao
) {
    val allSiswa: Flow<List<Siswa>> = siswaDao.getAllSiswa()

    fun getSiswaByKelompok(kelompok: String): Flow<List<Siswa>> =
        siswaDao.getSiswaByKelompok(kelompok)

    suspend fun getSiswaById(id: Int): Siswa? = withContext(Dispatchers.IO) {
        siswaDao.getSiswaById(id)
    }

    suspend fun insertSiswa(siswa: Siswa): Long = withContext(Dispatchers.IO) {
        siswaDao.insertSiswa(siswa)
    }

    suspend fun deleteSiswa(siswa: Siswa) = withContext(Dispatchers.IO) {
        siswaDao.deleteSiswa(siswa)
    }

    fun getObservasiForSiswa(siswaId: Int): Flow<List<ObservasiHarian>> =
        observasiDao.getObservasiForSiswa(siswaId)

    suspend fun getObservasiForSiswaOnDate(siswaId: Int, tanggal: String): ObservasiHarian? =
        withContext(Dispatchers.IO) {
            observasiDao.getObservasiForSiswaOnDate(siswaId, tanggal)
        }

    suspend fun insertObservasi(observasi: ObservasiHarian): Long = withContext(Dispatchers.IO) {
        observasiDao.insertObservasi(observasi)
    }

    suspend fun deleteObservasi(observasi: ObservasiHarian) = withContext(Dispatchers.IO) {
        observasiDao.deleteObservasi(observasi)
    }

    suspend fun seedDataIfEmpty() = withContext(Dispatchers.IO) {
        // We'll check if there are already students. If so, don't seed.
        val existing = siswaDao.getAllSiswa()
        // Simple way: check with a first/one-shot fetch or just insert if count is 0
        // To be safe, we can check count of existing or just check if a certain student exists.
        // Let's check if the table has any rows.
        var hasStudents = false
        try {
            // Retrieve first item from Flow on a separate coroutine or via simple query.
            // Since we don't have a direct count query, we can query a specific ID or do a small query.
            val testSiswa = siswaDao.getSiswaById(1)
            if (testSiswa != null) {
                hasStudents = true
            }
        } catch (e: Exception) {
            Log.e("ObservationRepository", "Error checking DB size", e)
        }

        if (!hasStudents) {
            Log.d("ObservationRepository", "Seeding database with rich sample data...")
            // Seed Students
            val malikId = siswaDao.insertSiswa(Siswa(id = 1, nis = "20260401", namaLengkap = "Ahmad Malik", kelompokUsia = "KB-B"))
            val bilqisId = siswaDao.insertSiswa(Siswa(id = 2, nis = "20260402", namaLengkap = "Bilqis Khaira", kelompokUsia = "KB-B"))
            val citraId = siswaDao.insertSiswa(Siswa(id = 3, nis = "20260403", namaLengkap = "Citra Lestari", kelompokUsia = "KB-B"))
            val dimasId = siswaDao.insertSiswa(Siswa(id = 4, nis = "20260404", namaLengkap = "Dimas Aditya", kelompokUsia = "KB-B"))
            val ekaId = siswaDao.insertSiswa(Siswa(id = 5, nis = "20260405", namaLengkap = "Eka Saputra", kelompokUsia = "KB-B"))
            
            val farhanId = siswaDao.insertSiswa(Siswa(id = 6, nis = "20260301", namaLengkap = "Farhan Ramadhan", kelompokUsia = "KB-A"))
            val giskaId = siswaDao.insertSiswa(Siswa(id = 7, nis = "20260302", namaLengkap = "Giska Amalia", kelompokUsia = "KB-A"))
            val hafizId = siswaDao.insertSiswa(Siswa(id = 8, nis = "20260303", namaLengkap = "Hafiz Pratama", kelompokUsia = "KB-A"))

            // Seed Observation history for Ahmad Malik (id = 1) - Multiple days
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val cal = Calendar.getInstance()

            // 5 days ago
            cal.add(Calendar.DAY_OF_YEAR, -5)
            observasiDao.insertObservasi(
                ObservasiHarian(
                    siswaId = 1,
                    guruId = "Bunda Sri",
                    tanggal = sdf.format(cal.time),
                    kegiatan = "Bermain balok kayu susun tinggi",
                    catatanAnekdot = "Malik sangat tekun menyusun 10 balok kayu membentuk menara tinggi tanpa terjatuh. Sempat miring namun Malik membetulkan posisi fondasi bawah dengan hati-hati.",
                    nilaiAgama = "BSH",
                    nilaiMotorik = "BSB",
                    nilaiKognitif = "BSB",
                    nilaiBahasa = "BSH",
                    nilaiSosem = "MB",
                    nilaiSeni = "BSH"
                )
            )

            // 4 days ago
            cal.add(Calendar.DAY_OF_YEAR, 1)
            observasiDao.insertObservasi(
                ObservasiHarian(
                    siswaId = 1,
                    guruId = "Bunda Sri",
                    tanggal = sdf.format(cal.time),
                    kegiatan = "Mewarnai gambar buah apel",
                    catatanAnekdot = "Malik bisa memegang krayon dengan cukup stabil, warna apel dominan merah di dalam garis, tapi daunnya keluar garis sedikit. Malik tersenyum bangga menunjukkan karyanya.",
                    nilaiAgama = "BSH",
                    nilaiMotorik = "BSH",
                    nilaiKognitif = "BSH",
                    nilaiBahasa = "BSH",
                    nilaiSosem = "BSH",
                    nilaiSeni = "BSB"
                )
            )

            // 3 days ago
            cal.add(Calendar.DAY_OF_YEAR, 1)
            observasiDao.insertObservasi(
                ObservasiHarian(
                    siswaId = 1,
                    guruId = "Bunda Sri",
                    tanggal = sdf.format(cal.time),
                    kegiatan = "Bermain estafet air dengan gelas plastik",
                    catatanAnekdot = "Malik berlari kencang memindahkan air, air tumpah sedikit tapi ia tidak menyerah. Namun saat giliran merapikan wadah bersama teman, Malik menolak dan langsung lari ke loker tas.",
                    nilaiAgama = "BSH",
                    nilaiMotorik = "BSB",
                    nilaiKognitif = "BSH",
                    nilaiBahasa = "BSH",
                    nilaiSosem = "BB",
                    nilaiSeni = "MB"
                )
            )

            // 2 days ago
            cal.add(Calendar.DAY_OF_YEAR, 1)
            observasiDao.insertObservasi(
                ObservasiHarian(
                    siswaId = 1,
                    guruId = "Bunda Sri",
                    tanggal = sdf.format(cal.time),
                    kegiatan = "Membaca doa sebelum makan bersama-sama",
                    catatanAnekdot = "Malik duduk dengan tenang, menengadahkan kedua tangan, dan melafalkan doa sebelum makan secara lengkap dan lantang bersama Bunda Sri.",
                    nilaiAgama = "BSB",
                    nilaiMotorik = "BSH",
                    nilaiKognitif = "BSH",
                    nilaiBahasa = "BSH",
                    nilaiSosem = "BSH",
                    nilaiSeni = "BSH"
                )
            )

            // 1 day ago
            cal.add(Calendar.DAY_OF_YEAR, 1)
            observasiDao.insertObservasi(
                ObservasiHarian(
                    siswaId = 1,
                    guruId = "Bunda Sri",
                    tanggal = sdf.format(cal.time),
                    kegiatan = "Menggambar garis bebas",
                    catatanAnekdot = "Malik asyik menggambar pola lingkaran dan kotak. Saat krayonnya dipinjam oleh Citra, Malik sempat memeluk krayonnya namun kemudian memberikan satu krayon warna biru dengan bantuan arahan guru.",
                    nilaiAgama = "BSH",
                    nilaiMotorik = "BSH",
                    nilaiKognitif = "BSH",
                    nilaiBahasa = "BSH",
                    nilaiSosem = "MB",
                    nilaiSeni = "BSB"
                )
            )

            // Seed Observation history for Bilqis Khaira (id = 2)
            cal.setTime(Calendar.getInstance().time)
            cal.add(Calendar.DAY_OF_YEAR, -3)
            observasiDao.insertObservasi(
                ObservasiHarian(
                    siswaId = 2,
                    guruId = "Bunda Lestari",
                    tanggal = sdf.format(cal.time),
                    kegiatan = "Senam fantasi meniru gerakan katak melompat",
                    catatanAnekdot = "Bilqis melompat dengan dua kaki seimbang dan sangat ceria mengikuti musik. Tertawa gembira sepanjang kegiatan.",
                    nilaiAgama = "BSB",
                    nilaiMotorik = "BSH",
                    nilaiKognitif = "BSH",
                    nilaiBahasa = "BSH",
                    nilaiSosem = "BSB",
                    nilaiSeni = "BSB"
                )
            )

            cal.add(Calendar.DAY_OF_YEAR, 1)
            observasiDao.insertObservasi(
                ObservasiHarian(
                    siswaId = 2,
                    guruId = "Bunda Lestari",
                    tanggal = sdf.format(cal.time),
                    kegiatan = "Membaca dongeng kelinci dan kura-kura",
                    catatanAnekdot = "Bilqis mendengarkan dongeng dengan saksama. Saat ditanya kembali jalannya cerita, Bilqis tersenyum malu-malu dan berbisik menjawab pelan ke arah Bunda.",
                    nilaiAgama = "BSB",
                    nilaiMotorik = "BSH",
                    nilaiKognitif = "BSH",
                    nilaiBahasa = "MB",
                    nilaiSosem = "BSH",
                    nilaiSeni = "BSH"
                )
            )

            cal.add(Calendar.DAY_OF_YEAR, 1)
            observasiDao.insertObservasi(
                ObservasiHarian(
                    siswaId = 2,
                    guruId = "Bunda Lestari",
                    tanggal = sdf.format(cal.time),
                    kegiatan = "Bermain balok kayu bersama Citra",
                    catatanAnekdot = "Bilqis berbagi balok segitiga secara mandiri ketika Citra memintanya. Bilqis juga memakai sepatunya sendiri dengan rapi setelah selesai bermain.",
                    nilaiAgama = "BSB",
                    nilaiMotorik = "BSH",
                    nilaiKognitif = "BSH",
                    nilaiBahasa = "BSH",
                    nilaiSosem = "BSB",
                    nilaiSeni = "BSH"
                )
            )
            
            Log.d("ObservationRepository", "Database seeding complete!")
        }
    }
}
