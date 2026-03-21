package org.delcom.pam_proyek1_ifs23004.ui.screens.internships

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import org.delcom.pam_proyek1_ifs23004.helper.ConstHelper
import org.delcom.pam_proyek1_ifs23004.helper.RouteHelper
import org.delcom.pam_proyek1_ifs23004.helper.SuspendHelper
import org.delcom.pam_proyek1_ifs23004.network.internships.data.CategoryEnum
import org.delcom.pam_proyek1_ifs23004.network.internships.data.LocationEnum
import org.delcom.pam_proyek1_ifs23004.network.internships.data.ResponseInternshipData
import org.delcom.pam_proyek1_ifs23004.ui.components.BottomNavComponent
import org.delcom.pam_proyek1_ifs23004.ui.components.LoadingUI
import org.delcom.pam_proyek1_ifs23004.ui.components.TopAppBarComponent
import org.delcom.pam_proyek1_ifs23004.ui.viewmodels.*

@Composable
fun InternshipEditScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    authViewModel: AuthViewModel,
    internshipViewModel: InternshipViewModel,
    internshipId: String
) {
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val uiStateInternship by internshipViewModel.uiState.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var internship by remember { mutableStateOf<ResponseInternshipData?>(null) }
    var authToken by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true

        if (uiStateAuth.auth !is AuthUIState.Success) {
            RouteHelper.to(navController, ConstHelper.RouteNames.Home.path, true)
            return@LaunchedEffect
        }

        authToken = (uiStateAuth.auth as AuthUIState.Success).data.authToken
        internshipViewModel.getInternshipById(authToken!!, internshipId)
    }

    LaunchedEffect(uiStateInternship.internship) {
        when (val state = uiStateInternship.internship) {
            is InternshipUIState.Success -> {
                internship = state.data
                isLoading = false
            }
            is InternshipUIState.Error -> {
                RouteHelper.back(navController)
                isLoading = false
            }
            else -> {}
        }
    }

    fun onSave(
        title: String,
        description: String,
        category: String,
        location: String,
        duration: String,
        requirement: String,
        benefit: String?,
        deadline: String
    ) {
        if (authToken == null) return
        isLoading = true

        internshipViewModel.putInternship(
            authToken = authToken!!,
            internshipId = internshipId,
            title = title,
            description = description,
            category = category,
            location = location,
            duration = duration,
            requirement = requirement,
            benefit = benefit,
            deadline = deadline
        )
    }

    LaunchedEffect(uiStateInternship.internshipChange) {
        when (val state = uiStateInternship.internshipChange) {
            is InternshipActionUIState.Success -> {
                SuspendHelper.showSnackBar(snackbarHost, SuspendHelper.SnackBarType.SUCCESS, state.message)
                RouteHelper.to(navController, ConstHelper.RouteNames.Internships.path, true)
                isLoading = false
            }
            is InternshipActionUIState.Error -> {
                SuspendHelper.showSnackBar(snackbarHost, SuspendHelper.SnackBarType.ERROR, state.message)
                isLoading = false
            }
            else -> {}
        }
    }

    if (isLoading || internship == null) {
        LoadingUI()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBarComponent(
            navController = navController,
            title = "Edit Lowongan",
            showBackButton = true,
        )
        Box(modifier = Modifier.weight(1f)) {
            InternshipEditUI(internship = internship!!, onSave = ::onSave)
        }
        BottomNavComponent(navController = navController)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InternshipEditUI(
    internship: ResponseInternshipData,
    onSave: (String, String, String, String, String, String, String?, String) -> Unit
) {
    var title by remember { mutableStateOf(internship.title) }
    var description by remember { mutableStateOf(internship.description) }
    var category by remember { mutableStateOf(internship.category) }
    var location by remember { mutableStateOf(internship.location) }
    var duration by remember { mutableStateOf(internship.duration) }
    var requirement by remember { mutableStateOf(internship.requirement) }
    var benefit by remember { mutableStateOf(internship.benefit ?: "") }
    var deadline by remember { mutableStateOf(internship.deadline) }

    var expandedCategory by remember { mutableStateOf(false) }
    var expandedLocation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Edit Informasi Lowongan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Lowongan") },
                    leadingIcon = { Icon(Icons.Default.Work, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori") },
                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        CategoryEnum.getAllFullNames().forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expandedLocation,
                    onExpandedChange = { expandedLocation = !expandedLocation },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = location,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Lokasi") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLocation) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedLocation,
                        onDismissRequest = { expandedLocation = false }
                    ) {
                        LocationEnum.getAllValues().forEach { loc ->
                            DropdownMenuItem(
                                text = { Text(loc) },
                                onClick = {
                                    location = loc
                                    expandedLocation = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Durasi Magang") },
                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text("Deadline Pendaftaran") },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = benefit,
                    onValueChange = { benefit = it },
                    label = { Text("Benefit (Opsional)") },
                    leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = requirement,
                    onValueChange = { requirement = it },
                    label = { Text("Kualifikasi") },
                    leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 4
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi Pekerjaan") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 6
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onSave(title, description, category, location, duration, requirement, benefit.takeIf { it.isNotBlank() }, deadline)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(Icons.Default.Save, contentDescription = "Simpan")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Simpan Perubahan", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}