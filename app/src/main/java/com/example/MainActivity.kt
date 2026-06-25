package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Siswa
import com.example.data.model.ObservasiHarian
import com.example.ui.theme.*
import com.example.ui.viewmodel.ObservationViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: ObservationViewModel = viewModel()) {
    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    
    val currentKelompok by viewModel.currentKelompok.collectAsStateWithLifecycle()
    val siswaList by viewModel.filteredSiswaList.collectAsStateWithLifecycle()
    val selectedSiswa by viewModel.selectedSiswa.collectAsStateWithLifecycle()
    val activeObservasi by viewModel.activeObservasi.collectAsStateWithLifecycle()
    val currentClassObservations by viewModel.currentClassObservations.collectAsStateWithLifecycle()

    var showAddStudentDialog by remember { mutableStateOf(false) }

    // Synchronize selected tab if user taps a student on the dashboard
    val onStudentSelected: (Siswa) -> Unit = { siswa ->
        viewModel.selectSiswa(siswa)
        selectedTab = 1 // Switch to the Observation / Input tab
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Column {
                            Text(
                                text = "e-Observasi KB Menur",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = "Jatikuwung • Zero-Friction AI PAUD",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            )
                        }
                    }
                },
                actions = {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (currentKelompok == "KB-B") "Bunda Sri" else "Bunda Lestari", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(imageVector = if (selectedTab == 0) Icons.Default.Dashboard else Icons.Outlined.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dasbor") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(imageVector = if (selectedTab == 1) Icons.Default.EditCalendar else Icons.Outlined.EditCalendar, contentDescription = "Input") },
                    label = { Text("Input Observasi") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(imageVector = if (selectedTab == 2) Icons.Default.AutoAwesome else Icons.Outlined.AutoAwesome, contentDescription = "Laporan AI") },
                    label = { Text("Laporan AI") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    currentKelompok = currentKelompok,
                    siswaList = siswaList,
                    observations = currentClassObservations,
                    onKelompokSelected = { viewModel.setKelompok(it) },
                    onStudentClick = onStudentSelected,
                    onAddStudentClick = { showAddStudentDialog = true }
                )
                1 -> ObservationInputScreen(
                    selectedSiswa = selectedSiswa,
                    activeObservasi = activeObservasi,
                    onBackClick = { selectedTab = 0 },
                    onUpdateObservasi = { viewModel.updateActiveObservasi(it) },
                    onSaveClick = {
                        viewModel.saveActiveObservasi()
                        Toast.makeText(context, "Observasi harian berhasil disimpan!", Toast.LENGTH_SHORT).show()
                        selectedTab = 0 // return to dashboard
                    },
                    siswaList = siswaList,
                    onStudentSelected = { viewModel.selectSiswa(it) }
                )
                2 -> LaporanAiScreen(
                    selectedSiswa = selectedSiswa,
                    siswaList = siswaList,
                    viewModel = viewModel,
                    onStudentSelected = { viewModel.selectSiswa(it) }
                )
            }

            if (showAddStudentDialog) {
                AddStudentDialog(
                    currentKelompok = currentKelompok,
                    onDismiss = { showAddStudentDialog = false },
                    onConfirm = { name, nis, kelompok ->
                        viewModel.addNewStudent(name, nis, kelompok)
                        showAddStudentDialog = false
                        Toast.makeText(context, "Siswa $name berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun DashboardScreen(
    currentKelompok: String,
    siswaList: List<Siswa>,
    observations: Map<Int, ObservasiHarian>,
    onKelompokSelected: (String) -> Unit,
    onStudentClick: (Siswa) -> Unit,
    onAddStudentClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredList = siswaList.filter {
        it.namaLengkap.contains(searchQuery, ignoreCase = true) || it.nis.contains(searchQuery)
    }

    val totalAnak = filteredList.size
    val diisiHariIni = filteredList.count { observations.containsKey(it.id) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
    ) {
        // Welcome Hero Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "Halo, Selamat Mengajar! 👋",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pencatatan observasi KB Menur Jatikuwung kini lebih praktis & didukung asisten AI pintar.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        }

        // Segmented Kelompok Selector & Add Student Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(3.dp)
                ) {
                    val items = listOf("KB-A", "KB-B")
                    items.forEach { item ->
                        val isSelected = currentKelompok == item
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                )
                                .clickable { onKelompokSelected(item) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (item == "KB-A") "KB-A (2-3 th)" else "KB-B (3-4 th)",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Button(
                    onClick = onAddStudentClick,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("add_student_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Siswa", fontSize = 13.sp)
                }
            }
        }

        // Stats Card Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.People, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Total Siswa", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text("$totalAnak Anak", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ScaleBSB.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ScaleBSB)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Terobservasi", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text("$diisiHariIni / $totalAnak", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (diisiHariIni == totalAnak) ScaleBSB else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().testTag("search_siswa_input"),
                placeholder = { Text("Cari nama anak atau NIS...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true
            )
        }

        // Section Title
        item {
            Text(
                text = "Daftar Siswa (Ketuk untuk Observasi)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Student Card List
        if (filteredList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.SentimentDissatisfied, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Tidak ada siswa ditemukan", fontWeight = FontWeight.SemiBold)
                        Text("Coba cari dengan kata kunci lain atau tambahkan siswa baru.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            items(filteredList) { siswa ->
                val isObserved = observations.containsKey(siswa.id)
                val todayObs = observations[siswa.id]

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onStudentClick(siswa) }
                        .testTag("siswa_card_${siswa.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isObserved) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = if (isObserved) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            // Student Initial Avatar
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isObserved) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = siswa.namaLengkap.take(2).uppercase(),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = if (isObserved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = siswa.namaLengkap,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "NIS: ${siswa.nis}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        }

                        // Status indicator
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isObserved) ScaleBSB.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isObserved) {
                                    val scales = listOfNotNull(
                                        todayObs?.nilaiAgama,
                                        todayObs?.nilaiMotorik,
                                        todayObs?.nilaiKognitif,
                                        todayObs?.nilaiBahasa,
                                        todayObs?.nilaiSosem,
                                        todayObs?.nilaiSeni
                                    )
                                    if (scales.isNotEmpty()) "Sudah Diisi - ${scales.first()}" else "Sudah Diisi"
                                } else "Belum Diisi",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isObserved) ScaleBSB else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ObservationInputScreen(
    selectedSiswa: Siswa?,
    activeObservasi: ObservasiHarian?,
    onBackClick: () -> Unit,
    onUpdateObservasi: ((ObservasiHarian) -> ObservasiHarian) -> Unit,
    onSaveClick: () -> Unit,
    siswaList: List<Siswa>,
    onStudentSelected: (Siswa) -> Unit
) {
    if (selectedSiswa == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.HowToReg,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Belum Ada Siswa Terpilih",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Silakan pilih salah satu nama siswa di bawah ini untuk memulai input observasi harian.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            
            // Fast selection grid
            Text("Pilih Cepat Siswa:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                maxItemsInEachRow = 3
            ) {
                siswaList.take(6).forEach { siswa ->
                    SuggestionChip(
                        onClick = { onStudentSelected(siswa) },
                        label = { Text(siswa.namaLengkap.split(" ")[0]) },
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
        return
    }

    if (activeObservasi == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
    ) {
        // Selected Student details
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(selectedSiswa.namaLengkap, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("NIS: ${selectedSiswa.nis} • ${selectedSiswa.kelompokUsia}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    OutlinedButton(
                        onClick = onBackClick,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Text("Ganti", fontSize = 12.sp)
                    }
                }
            }
        }

        // Section header
        item {
            Text(
                text = "1. Informasi Kegiatan Harian",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }

        // Form Inputs
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = activeObservasi.kegiatan,
                    onValueChange = { newValue ->
                        onUpdateObservasi { it.copy(kegiatan = newValue) }
                    },
                    label = { Text("Nama Kegiatan") },
                    placeholder = { Text("Misal: Bermain balok kayu, mewarnai gambar buah") },
                    modifier = Modifier.fillMaxWidth().testTag("kegiatan_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = activeObservasi.catatanAnekdot,
                    onValueChange = { newValue ->
                        onUpdateObservasi { it.copy(catatanAnekdot = newValue) }
                    },
                    label = { Text("Catatan Anekdot (Opsional)") },
                    placeholder = { Text("Ceritakan perilaku menonjol atau reaksi anak secara singkat...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("catatan_anekdot_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    maxLines = 4
                )
            }
        }

        // Assessment Header
        item {
            Text(
                text = "2. Penilaian 6 Aspek Capaian PAUD",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Aspect rows
        val aspects = listOf(
            AspectDefinition("Agama", "Nilai Agama & Budi Pekerti", activeObservasi.nilaiAgama) { scale ->
                onUpdateObservasi { it.copy(nilaiAgama = scale) }
            },
            AspectDefinition("Motorik", "Fisik Motorik (Kasar & Halus)", activeObservasi.nilaiMotorik) { scale ->
                onUpdateObservasi { it.copy(nilaiMotorik = scale) }
            },
            AspectDefinition("Kognitif", "Kemampuan Berpikir Logis", activeObservasi.nilaiKognitif) { scale ->
                onUpdateObservasi { it.copy(nilaiKognitif = scale) }
            },
            AspectDefinition("Bahasa", "Kemampuan Berbicara & Paham", activeObservasi.nilaiBahasa) { scale ->
                onUpdateObservasi { it.copy(nilaiBahasa = scale) }
            },
            AspectDefinition("Sosem", "Sosial Emosional & Mandiri", activeObservasi.nilaiSosem) { scale ->
                onUpdateObservasi { it.copy(nilaiSosem = scale) }
            },
            AspectDefinition("Seni", "Aktivitas Musik & Mewarnai", activeObservasi.nilaiSeni) { scale ->
                onUpdateObservasi { it.copy(nilaiSeni = scale) }
            }
        )

        items(aspects) { aspect ->
            AspectAssessmentRow(
                aspectId = aspect.id,
                label = aspect.label,
                currentScale = aspect.currentScale,
                onScaleSelected = aspect.onScaleSelected
            )
        }

        // Save Button
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onSaveClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_observasi_button"),
                shape = RoundedCornerShape(14.dp),
                enabled = activeObservasi.kegiatan.isNotBlank()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simpan Observasi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

data class AspectDefinition(
    val id: String,
    val label: String,
    val currentScale: String?,
    val onScaleSelected: (String?) -> Unit
)

@Composable
fun AspectAssessmentRow(
    aspectId: String,
    label: String,
    currentScale: String?,
    onScaleSelected: (String?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val scales = listOf(
                    ScaleItem("BB", "Belum Berkembang", ScaleBB),
                    ScaleItem("MB", "Mulai Berkembang", ScaleMB),
                    ScaleItem("BSH", "Sesuai Harapan", ScaleBSH),
                    ScaleItem("BSB", "Sangat Baik", ScaleBSB)
                )

                scales.forEach { scale ->
                    val isSelected = currentScale == scale.code
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp) // min touch target height
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) scale.color.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f)
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) scale.color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                if (isSelected) onScaleSelected(null) else onScaleSelected(scale.code)
                            }
                            .padding(4.dp)
                            .testTag("scale_btn_${aspectId}_${scale.code}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = scale.code,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = if (isSelected) scale.color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = scale.shortLabel,
                                fontSize = 8.sp,
                                maxLines = 1,
                                color = if (isSelected) scale.color.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

data class ScaleItem(val code: String, val shortLabel: String, val color: Color)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LaporanAiScreen(
    selectedSiswa: Siswa?,
    siswaList: List<Siswa>,
    viewModel: ObservationViewModel,
    onStudentSelected: (Siswa) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val generatedReport by viewModel.generatedReport.collectAsStateWithLifecycle()
    val isGeneratingReport by viewModel.isGeneratingReport.collectAsStateWithLifecycle()

    var selectedReportType by remember { mutableStateOf("MINGGUAN") } // MINGGUAN, BULANAN, SEMESTER

    if (selectedSiswa == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Belum Ada Siswa Terpilih",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Silakan pilih salah satu siswa untuk menganalisis data observasi dan menyusun laporan naratif otomatis dengan AI.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            
            Text("Pilih Cepat Siswa:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                maxItemsInEachRow = 3
            ) {
                siswaList.take(6).forEach { siswa ->
                    SuggestionChip(
                        onClick = { onStudentSelected(siswa) },
                        label = { Text(siswa.namaLengkap.split(" ")[0]) },
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
        return
    }

    val historyState = viewModel.getSiswaObservationHistory(selectedSiswa.id).collectAsState(initial = emptyList())
    val history = historyState.value

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
    ) {
        // Selected student profile card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Face, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(selectedSiswa.namaLengkap, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("NIS: ${selectedSiswa.nis} • ${selectedSiswa.kelompokUsia}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Button(
                        onClick = {
                            // Quick change trigger
                            // Circular cycle through students
                            val nextIdx = (siswaList.indexOf(selectedSiswa) + 1) % siswaList.size
                            if (nextIdx >= 0 && nextIdx < siswaList.size) {
                                onStudentSelected(siswaList[nextIdx])
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Text("Siswa Lain", fontSize = 12.sp)
                    }
                }
            }
        }

        // Report Type Selector
        item {
            Column {
                Text(
                    text = "1. Pilih Jenis Laporan Perkembangan",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val reportTypes = listOf(
                        Triple("MINGGUAN", "Mingguan", Icons.Default.DateRange),
                        Triple("BULANAN", "Bulanan", Icons.Default.CalendarMonth),
                        Triple("SEMESTER", "Raport", Icons.Default.Description)
                    )

                    reportTypes.forEach { type ->
                        val isSelected = selectedReportType == type.first
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surface
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedReportType = type.first }
                                .padding(4.dp)
                                .testTag("report_type_btn_${type.first}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = type.third,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = type.second,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Preview Available Records
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "2. Riwayat Observasi Tersedia",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            Text("${history.size} Entri", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (history.isEmpty()) {
                        Text(
                            text = "Belum ada rekaman observasi harian. Silakan ketuk tab 'Input Observasi' untuk mencatat kegiatan harian anak terlebih dahulu.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    } else {
                        // Show up to 3 previews
                        history.take(3).forEach { obs ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = obs.kegiatan, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(text = obs.tanggal, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                                // Small indicator of aspect count
                                val evaluatedCount = listOfNotNull(obs.nilaiAgama, obs.nilaiMotorik, obs.nilaiKognitif, obs.nilaiBahasa, obs.nilaiSosem, obs.nilaiSeni).size
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    contentColor = MaterialTheme.colorScheme.primary
                                ) {
                                    Text("$evaluatedCount Aspek", fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp))
                                }
                            }
                            if (obs != history.take(3).last()) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                            }
                        }
                        if (history.size > 3) {
                            Text(
                                text = "+ ${history.size - 3} entri lainnya tersimpan di database local.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Action Trigger
        item {
            Button(
                onClick = {
                    viewModel.generateReportViaAI(selectedSiswa, selectedReportType, history)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("generate_report_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                enabled = !isGeneratingReport
            ) {
                if (isGeneratingReport) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("AI sedang menganalisis data...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Generate Laporan (AI Gemini)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // Generated Output Card
        if (isGeneratingReport || generatedReport != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Draf Laporan Hasil Olahan AI",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            if (generatedReport != null && !isGeneratingReport) {
                                Row {
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(generatedReport ?: ""))
                                            Toast.makeText(context, "Laporan disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Salin")
                                    }
                                    IconButton(
                                        onClick = {
                                            Toast.makeText(context, "Membuka menu cetak sistem... (Simulasi)", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Default.Print, contentDescription = "Cetak")
                                    }
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                        if (isGeneratingReport) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Membaca seluruh riwayat perkembangan harian...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = TextAlign.Center)
                                Text("Merumuskan deskripsi narasi standar akreditasi PAUD...", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), textAlign = TextAlign.Center)
                            }
                        } else {
                            Text(
                                text = parseMarkdownToAnnotatedString(generatedReport ?: ""),
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

// Rich Text helper to render simple Markdown nicely in Jetpack Compose
fun parseMarkdownToAnnotatedString(text: String): AnnotatedString {
    return buildAnnotatedString {
        val lines = text.split("\n")
        lines.forEachIndexed { index, line ->
            var lineProcessed = false
            
            // Format headers
            if (line.startsWith("### ")) {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF2E6F40))) {
                    append(line.removePrefix("### "))
                }
                lineProcessed = true
            } else if (line.startsWith("## ")) {
                withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF2E6F40))) {
                    append(line.removePrefix("## "))
                }
                lineProcessed = true
            } else if (line.startsWith("# ")) {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF2E6F40))) {
                    append(line.removePrefix("# "))
                }
                lineProcessed = true
            } else if (line.startsWith("**") && line.endsWith("**")) {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF2E6F40))) {
                    append(line.replace("**", ""))
                }
                lineProcessed = true
            } else if (line.startsWith("- ") || line.startsWith("* ")) {
                append("• ")
                val cleanLine = if (line.startsWith("- ")) line.removePrefix("- ") else line.removePrefix("* ")
                appendInlineBoldText(cleanLine)
                lineProcessed = true
            }

            if (!lineProcessed) {
                // Check inline bold markdown
                appendInlineBoldText(line)
            }

            if (index < lines.size - 1) {
                append("\n")
            }
        }
    }
}

fun AnnotatedString.Builder.appendInlineBoldText(line: String) {
    val parts = line.split("**")
    parts.forEachIndexed { partIdx, part ->
        if (partIdx % 2 == 1) {
            // This was surrounded by **
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF2E6F40))) {
                append(part)
            }
        } else {
            append(part)
        }
    }
}

@Composable
fun AddStudentDialog(
    currentKelompok: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, nis: String, kelompok: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var nis by remember { mutableStateOf("") }
    var kelompok by remember { mutableStateOf(currentKelompok) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Tambah Siswa Baru",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Lengkap Anak") },
                    placeholder = { Text("Misal: Ahmad Malik") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("add_student_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = nis,
                    onValueChange = { nis = it },
                    label = { Text("NIS (Nomor Induk Siswa)") },
                    placeholder = { Text("8 digit angka, misal: 20260401") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("add_student_nis_input"),
                    singleLine = true
                )

                Column {
                    Text("Kelompok Usia / Kelas:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("KB-A", "KB-B").forEach { cls ->
                            val isSelected = kelompok == cls
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { kelompok = cls }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (cls == "KB-A") "KB-A (2-3 th)" else "KB-B (3-4 th)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(name, nis, kelompok) },
                        shape = RoundedCornerShape(10.dp),
                        enabled = name.isNotBlank() && nis.isNotBlank()
                    ) {
                        Text("Simpan Siswa")
                    }
                }
            }
        }
    }
}
