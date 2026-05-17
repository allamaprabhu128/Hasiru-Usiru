package com.hasiruusiru.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.hasiruusiru.app.data.AppDatabase
import com.hasiruusiru.app.data.TreePoint
import com.hasiruusiru.app.data.TreeRepository
import com.hasiruusiru.app.model.SpeciesData
import com.hasiruusiru.app.viewmodel.MapViewModel

class MapViewModelFactory(private val repository: TreeRepository) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var database: AppDatabase
    private lateinit var repository: TreeRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        database = androidx.room.Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "hasiru_usiru_db"
        ).build()
        repository = TreeRepository(database.treeDao())

        setContent {
            HasiruUsiruTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel = viewModel(factory = MapViewModelFactory(repository)))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MapViewModel = viewModel()) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val oxygenScore by viewModel.totalOxygenScore.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Hasiru-Usiru", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Place, contentDescription = "Map") },
                    label = { Text("Map") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Info, contentDescription = "Guide") },
                    label = { Text("Guide") }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(onClick = { /* Could trigger camera for identity */ }) {
                    Icon(Icons.Default.Add, contentDescription = "Quick Tag")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> MapInterface(viewModel)
                1 -> SpeciesGuide()
            }
            
            // Dashboard Overlay
            if (selectedTab == 0) {
                OxygenDashboard(oxygenScore)
            }
        }
    }
}

@Composable
fun OxygenDashboard(score: Double) {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Total Oxygen Score", style = MaterialTheme.typography.labelLarge)
            Text(
                String.format("%.2f", score),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF2E7D32) // Forest Green
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapInterface(viewModel: MapViewModel) {
    val treePoints by viewModel.treePoints.collectAsState()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(12.9716, 77.5946), 12f) // Bangalore coordinates
    }

    var showBottomSheet by remember { mutableStateOf(false) }
    var lastDroppedLatLng by remember { mutableStateOf<LatLng?>(null) }

    val sheetState = rememberModalBottomSheetState()

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapLongClick = { latLng ->
            lastDroppedLatLng = latLng
            showBottomSheet = true
        }
    ) {
        // Here we would use MarkerClustering if the library is integrated.
        // For demonstration, we'll use individual markers.
        treePoints.forEach { point ->
            Marker(
                state = MarkerState(position = LatLng(point.latitude, point.longitude)),
                title = point.species,
                snippet = "Girth: ${point.girth}cm | Score: ${viewModel.calculateOxygenScore(point.girth, viewModel.getSpeciesFactor(point.species))}",
                icon = BitmapDescriptorFactory.defaultMarker(
                    if (point.type == "Tree Tagger") BitmapDescriptorFactory.HUE_GREEN 
                    else BitmapDescriptorFactory.HUE_RED
                )
            )
        }
    }

    if (showBottomSheet && lastDroppedLatLng != null) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            TreeDataForm(
                latLng = lastDroppedLatLng!!,
                onSave = { treePoint ->
                    viewModel.addTreePoint(treePoint)
                    showBottomSheet = false
                },
                getFactor = { viewModel.getSpeciesFactor(it) }
            )
        }
    }
}

@Composable
fun TreeDataForm(latLng: LatLng, onSave: (TreePoint) -> Unit, getFactor: (String) -> Double) {
    var selectedSpecies by remember { mutableStateOf("Neem") }
    var girth by remember { mutableFloatStateOf(10f) }
    var healthStatus by remember { mutableStateOf("Healthy") }
    var markerType by remember { mutableStateOf("Tree Tagger") }

    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth()
    ) {
        Text("Tag New Location", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        // Marker Type
        Text("Tag Type")
        Row {
            RadioButton(selected = markerType == "Tree Tagger", onClick = { markerType = "Tree Tagger" })
            Text("Tree Tagger", modifier = Modifier.align(Alignment.CenterVertically))
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(selected = markerType == "Empty Pit", onClick = { markerType = "Empty Pit" })
            Text("Empty Pit", modifier = Modifier.align(Alignment.CenterVertically))
        }

        if (markerType == "Tree Tagger") {
            // Species Dropdown Placeholder
            Text("Species: $selectedSpecies")
            Slider(value = girth, onValueChange = { girth = it }, valueRange = 1f..200f)
            Text("Estimated Girth: ${girth.toInt()} cm")
            
            Text("Health Status")
            Row {
                RadioButton(selected = healthStatus == "Healthy", onClick = { healthStatus = "Healthy" })
                Text("Healthy")
                RadioButton(selected = healthStatus == "Sick", onClick = { healthStatus = "Sick" })
                Text("Sick")
            }

            val currentScore = girth.toDouble() * getFactor(selectedSpecies)
            Text(
                "Estimated Oxygen Contribution: ${String.format("%.2f", currentScore)}",
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.Bold
            )
        }

        Button(
            onClick = {
                onSave(
                    TreePoint(
                        latitude = latLng.latitude,
                        longitude = latLng.longitude,
                        species = if (markerType == "Tree Tagger") selectedSpecies else "N/A",
                        girth = girth.toDouble(),
                        healthStatus = healthStatus,
                        type = markerType
                    )
                )
            },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Save Location")
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SpeciesGuide() {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Native Species Guide", style = MaterialTheme.typography.headlineMedium)
            Text("Native trees of Karnataka and their benefits", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(SpeciesData.nativeTrees) { species ->
            SpeciesCard(species)
        }
    }
}

@Composable
fun SpeciesCard(species: com.hasiruusiru.app.model.Species) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(species.nameEnglish, style = MaterialTheme.typography.titleLarge)
                    Text(species.nameScientific, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    species.nameKannada, 
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Benefit (EN): ${species.benefitEnglish}", style = MaterialTheme.typography.bodyMedium)
            Text("ಪ್ರಯೋಜನ (KN): ${species.benefitKannada}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun HasiruUsiruTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF2E7D32),
            secondary = Color(0xFF81C784),
            tertiary = Color(0xFFFFF176)
        ),
        content = content
    )
}
