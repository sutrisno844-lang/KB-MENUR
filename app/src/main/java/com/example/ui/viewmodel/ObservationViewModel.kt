package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiApiClient
import com.example.data.database.AppDatabase
import com.example.data.model.Siswa
import com.example.data.model.ObservasiHarian
import com.example.data.repository.ObservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ObservationViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = ObservationRepository(db.siswaDao(), db.observasiDao())

    val currentKelompok = MutableStateFlow("KB-B") // Default is KB-B (Usia 3-4 Tahun)

    // Reactive list of students based on selected Kelompok
    val filteredSiswaList: StateFlow<List<Siswa>> = currentKelompok
        .flatMapLatest { kelompok ->
            repository.getSiswaByKelompok(kelompok)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val selectedSiswa = MutableStateFlow<Siswa?>(null)
    val activeObservasi = MutableStateFlow<ObservasiHarian?>(null)
    
    val isGeneratingReport = MutableStateFlow(false)
    val generatedReport = MutableStateFlow<String?>(null)

    // Track daily completion status for the class
    val currentClassObservations = MutableStateFlow<Map<Int, ObservasiHarian>>(emptyMap())

    init {
        viewModelScope.launch {
            // Seed sample database if empty
            repository.seedDataIfEmpty()
            
            // Collect today's completion status
            loadTodayCompletionStatus()
        }
    }

    fun setKelompok(kelompok: String) {
        currentKelompok.value = kelompok
        viewModelScope.launch {
            loadTodayCompletionStatus()
        }
    }

    fun selectSiswa(siswa: Siswa) {
        selectedSiswa.value = siswa
        generatedReport.value = null
        loadActiveObservasiForToday(siswa.id)
    }

    private val todayDateString: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private fun loadActiveObservasiForToday(siswaId: Int) {
        viewModelScope.launch {
            val date = todayDateString
            val obs = repository.getObservasiForSiswaOnDate(siswaId, date)
            if (obs != null) {
                activeObservasi.value = obs
            } else {
                // Initialize new empty observation
                activeObservasi.value = ObservasiHarian(
                    siswaId = siswaId,
                    guruId = if (currentKelompok.value == "KB-B") "Bunda Sri" else "Bunda Lestari",
                    tanggal = date,
                    kegiatan = "",
                    catatanAnekdot = ""
                )
            }
        }
    }

    fun updateActiveObservasi(updater: (ObservasiHarian) -> ObservasiHarian) {
        activeObservasi.value = activeObservasi.value?.let(updater)
    }

    fun saveActiveObservasi() {
        val obs = activeObservasi.value ?: return
        viewModelScope.launch {
            repository.insertObservasi(obs)
            loadTodayCompletionStatus()
        }
    }

    suspend fun loadTodayCompletionStatus() {
        val date = todayDateString
        viewModelScope.launch {
            // Query for all observations and filter or build status map
            // Since this is a prototype, we'll fetch from allSiswa flow and build a simple mapping
            repository.allSiswa.collect { list ->
                val statusMap = mutableMapOf<Int, ObservasiHarian>()
                for (siswa in list) {
                    val obs = repository.getObservasiForSiswaOnDate(siswa.id, date)
                    if (obs != null) {
                        statusMap[siswa.id] = obs
                    }
                }
                currentClassObservations.value = statusMap
            }
        }
    }

    fun addNewStudent(nama: String, nis: String, kelompok: String) {
        viewModelScope.launch {
            repository.insertSiswa(
                Siswa(
                    nis = nis,
                    namaLengkap = nama,
                    kelompokUsia = kelompok
                )
            )
        }
    }

    fun getSiswaObservationHistory(siswaId: Int) = repository.getObservasiForSiswa(siswaId)

    fun generateReportViaAI(siswa: Siswa, type: String, history: List<ObservasiHarian>) {
        if (history.isEmpty()) {
            generatedReport.value = "Belum ada riwayat observasi untuk ${siswa.namaLengkap}. Silakan tambahkan minimal 1 observasi harian terlebih dahulu agar AI dapat membaca datanya."
            return
        }

        isGeneratingReport.value = true
        generatedReport.value = null

        viewModelScope.launch {
            // Build the observation summary text to feed into Gemini
            val dataStr = history.joinToString("\n\n") { obs ->
                """
                Tanggal: ${obs.tanggal}
                Kegiatan: ${obs.kegiatan}
                Catatan Anekdot: ${obs.catatanAnekdot}
                Penilaian Aspek:
                - Agama: ${obs.nilaiAgama ?: "-"}
                - Fisik Motorik: ${obs.nilaiMotorik ?: "-"}
                - Kognitif: ${obs.nilaiKognitif ?: "-"}
                - Bahasa: ${obs.nilaiBahasa ?: "-"}
                - Sosial Emosional (Sosem): ${obs.nilaiSosem ?: "-"}
                - Seni: ${obs.nilaiSeni ?: "-"}
                """.trimIndent()
            }

            val prompt = when (type) {
                "MINGGUAN" -> """
                    Tolong buatkan LAPORAN MINGGUAN PERKEMBANGAN ANAK di KB Menur Jatikuwung untuk siswa berikut:
                    Nama Anak: ${siswa.namaLengkap}
                    Kelompok: ${siswa.kelompokUsia}
                    
                    Berikut adalah data observasi harian minggu ini:
                    $dataStr
                    
                    Format Laporan harus rapi menggunakan Markdown, menyertakan:
                    1. Header Laporan Mingguan KB Menur Jatikuwung
                    2. Ringkasan perkembangan minggu ini secara umum dalam bahasa ramah dan mendidik
                    3. Aspek yang Menonjol (berdasarkan nilai BSB/BSH)
                    4. Aspek yang Perlu Stimulasi (berdasarkan nilai BB/MB)
                    5. Rekomendasi Kegiatan di Rumah untuk Orang Tua
                    
                    Gunakan gaya bahasa santun, ramah, penuh kasih sayang khas guru PAUD (panggil anak dengan "Ananda ${siswa.namaLengkap.split(" ")[0]}").
                """.trimIndent()

                "BULANAN" -> """
                    Tolong buatkan REKAPITULASI BULANAN & PORTFOLIO DIGITAL di KB Menur Jatikuwung untuk siswa berikut:
                    Nama Anak: ${siswa.namaLengkap}
                    Kelompok: ${siswa.kelompokUsia}
                    
                    Berikut adalah data observasi harian bulan ini:
                    $dataStr
                    
                    Format Laporan harus rapi menggunakan Markdown, menyertakan:
                    1. Header Rekapitulasi Bulanan KB Menur Jatikuwung
                    2. Tabel capaian per aspek perkembangan (Agama, Fisik Motorik, Kognitif, Bahasa, Sosem, Seni) dengan skala akhir (BB/MB/BSH/BSB) yang disimpulkan dari data observasi harian, beserta deskripsi ringkasnya.
                    3. Catatan Guru secara keseluruhan untuk bulan ini.
                    4. Portofolio Karya Terbaik (pilih kegiatan yang paling menonjol dari data observasi dan jelaskan mengapa itu terpilih sebagai karya terbaik).
                    
                    Gunakan bahasa profesional namun tetap ramah khas guru PAUD.
                """.trimIndent()

                else -> """
                    Tolong buatkan draf LAPORAN CAPAIAN PERKEMBANGAN SEMESTER (RAPORT RESMI PAUD) di KB Menur Jatikuwung untuk siswa berikut:
                    Nama Anak: ${siswa.namaLengkap}
                    NIS: ${siswa.nis}
                    Kelompok: ${siswa.kelompokUsia}
                    
                    Berikut adalah seluruh riwayat data observasi semester ini:
                    $dataStr
                    
                    Format Raport harus sama persis atau mengikuti struktur resmi berikut dalam Markdown yang sangat rapi:
                    ==========================================
                    LAPORAN CAPAIAN PERKEMBANGAN ANAK (RAPORT)
                           KB MENUR JATIKUWUNG
                    ==========================================
                    Nama Anak   : ${siswa.namaLengkap}
                    NIS         : ${siswa.nis}
                    Kelompok    : ${siswa.kelompokUsia}
                    Semester    : 1 (Ganjil)
                    Tahun Ajar  : 2026/2027

                    1. Nilai Agama dan Budi Pekerti
                    ---------------------------------------------------------------------------------
                    (Uraikan perkembangan dengan detail, hubungkan dengan data observasi aspek Agama. Gunakan istilah perkembangannya: Belum Berkembang (BB), Mulai Berkembang (MB), Berkembang Sesuai Harapan (BSH), atau Berkembang Sangat Baik (BSB))

                    2. Fisik Motorik
                    ---------------------------------------------------------------------------------
                    (Uraikan perkembangan motorik kasar dan halus secara detail dari data observasi)

                    3. Kognitif, Bahasa, & Sosem (Deskripsi Otomatis AI)
                    ---------------------------------------------------------------------------------
                    (Uraikan berpikir logis, kemampuan bahasa, kemandirian, sosialisasi, dan seni anak secara komprehensif)

                    Saran Tindak Lanjut:
                    ---------------------------------------------------------------------------------
                    (Berikan rekomendasi praktis untuk stimulasi di rumah oleh Ayah/Bunda)

                    Kehadiran:
                    - Sakit: 1 hari (Simulasi)
                    - Izin : 0 hari (Simulasi)
                    - Alpa : 0 hari (Simulasi)
                    
                                                    Jatikuwung, 18 Desember 2026
                    Mengetahui,
                    Kepala Sekolah                              Guru Kelas
                    
                    (Sri Wahyuni, S.Pd.)                     (Bunda Sri, S.Pd.)
                    ==========================================

                    Harap isi setiap bagian narasi dengan deskripsi yang hangat, mendalam, dan mendidik berdasarkan data observasi riil yang diberikan.
                """.trimIndent()
            }

            val systemInstruction = "Anda adalah Asisten AI untuk guru-guru PAUD di KB Menur Jatikuwung Jawa Tengah. Tugas Anda adalah membantu guru menganalisis observasi harian anak usia dini dan memformulasikan laporan naratif perkembangan anak yang profesional, menyentuh hati, terstruktur, ramah, dan mendidik menggunakan istilah standar PAUD Indonesia (BB, MB, BSH, BSB)."

            val result = GeminiApiClient.generateReport(prompt, systemInstruction)
            generatedReport.value = result
            isGeneratingReport.value = false
        }
    }
}
