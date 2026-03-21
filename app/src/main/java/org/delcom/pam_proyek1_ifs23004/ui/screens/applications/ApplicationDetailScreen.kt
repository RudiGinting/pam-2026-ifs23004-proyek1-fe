package org.delcom.pam_proyek1_ifs23004.ui.screens.applications

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.delcom.pam_proyek1_ifs23004.helper.ConstHelper
import org.delcom.pam_proyek1_ifs23004.helper.RouteHelper
import org.delcom.pam_proyek1_ifs23004.helper.ToolsHelper
import org.delcom.pam_proyek1_ifs23004.network.internships.data.ResponseApplicationData
import org.delcom.pam_proyek1_ifs23004.network.internships.data.ResponseInternshipData
import org.delcom.pam_proyek1_ifs23004.ui.components.*
import org.delcom.pam_proyek1_ifs23004.ui.viewmodels.*

@Composable
fun ApplicationDetailScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    authViewModel: AuthViewModel,
    internshipViewModel: InternshipViewModel,
    applicationId: String
) {
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val uiStateInternship by internshipViewModel.uiState.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var application by remember { mutableStateOf<ResponseApplicationData?>(null) }
    var internship by remember { mutableStateOf<ResponseInternshipData?>(null) }
    var authToken by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        isLoading = true

        if (uiStateAuth.auth !is AuthUIState.Success) {
            RouteHelper.to(navController, ConstHelper.RouteNames.Home.path, true)
            return@LaunchedEffect
        }

        authToken = (uiStateAuth.auth as AuthUIState.Success).data.authToken
        internshipViewModel.getMyApplications(authToken!!, 1, 100)
    }

    LaunchedEffect(uiStateInternship.myApplications) {
        when (val state = uiStateInternship.myApplications) {
            is ApplicationsUIState.Success -> {
                val foundApplication = state.data.find { it.id == applicationId }
                if (foundApplication != null) {
                    application = foundApplication
                    internshipViewModel.getInternshipById(authToken!!, foundApplication.internshipId)
                } else {
                    Toast.makeText(context, "Lamaran tidak ditemukan", Toast.LENGTH_SHORT).show()
                    RouteHelper.back(navController)
                    isLoading = false
                }
            }
            is ApplicationsUIState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                RouteHelper.back(navController)
                isLoading = false
            }
            else -> {}
        }
    }

    LaunchedEffect(uiStateInternship.internship) {
        when (val state = uiStateInternship.internship) {
            is InternshipUIState.Success -> {
                internship = state.data
                isLoading = false
            }
            is InternshipUIState.Error -> {
                isLoading = false
            }
            else -> {}
        }
    }

    fun onDelete() {
        if (authToken == null) return
        isLoading = true
        internshipViewModel.deleteApplication(authToken!!, applicationId)
    }

    LaunchedEffect(uiStateInternship.applicationDelete) {
        when (val state = uiStateInternship.applicationDelete) {
            is InternshipActionUIState.Success -> {
                Toast.makeText(context, "Lamaran berhasil dibatalkan", Toast.LENGTH_SHORT).show()
                RouteHelper.to(navController, ConstHelper.RouteNames.MyApplications.path, true)
                isLoading = false
            }
            is InternshipActionUIState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                isLoading = false
            }
            else -> {}
        }
    }

    if (isLoading || application == null) {
        LoadingUI()
        return
    }

    val safeApplication = application!!

    val menuItems = if (safeApplication.status == "pending") {
        listOf(
            TopAppBarMenuItem(
                text = "Batalkan Lamaran",
                icon = Icons.Filled.Delete,
                route = null,
                onClick = { showDeleteDialog = true },  // PERBAIKAN: hanya set showDeleteDialog = true
                isDestructive = true
            )
        )
    } else {
        emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBarComponent(
            navController = navController,
            title = "Detail Lamaran",
            showBackButton = true,
            customMenuItems = menuItems
        )
        Box(modifier = Modifier.weight(1f)) {
            ApplicationDetailUI(
                application = safeApplication,
                internship = internship,
                onViewInternship = {
                    RouteHelper.to(
                        navController,
                        ConstHelper.RouteNames.InternshipsDetail.path
                            .replace("{internshipId}", safeApplication.internshipId)
                    )
                }
            )
        }
        BottomNavComponent(navController = navController)
    }

    // Dialog Konfirmasi Batal
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Batalkan Lamaran") },
            text = { Text("Apakah Anda yakin ingin membatalkan lamaran ini?") },
            confirmButton = {
                Button(
                    onClick = { onDelete() },  // PERBAIKAN: panggil onDelete() di dalam lambda
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Ya, Batalkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Tidak")
                }
            }
        )
    }
}

@Composable
fun ApplicationDetailUI(
    application: ResponseApplicationData,
    internship: ResponseInternshipData?,
    onViewInternship: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header dengan Status
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(
                    when (application.status.lowercase()) {
                        "accepted" -> MaterialTheme.colorScheme.secondaryContainer
                        "rejected" -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.primaryContainer
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = when (application.status.lowercase()) {
                        "accepted" -> Icons.Filled.CheckCircle
                        "rejected" -> Icons.Filled.Cancel
                        else -> Icons.Filled.Schedule
                    },
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = when (application.status.lowercase()) {
                        "accepted" -> MaterialTheme.colorScheme.onSecondaryContainer
                        "rejected" -> MaterialTheme.colorScheme.onErrorContainer
                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when (application.status.lowercase()) {
                        "accepted" -> "Lamaran Diterima!"
                        "rejected" -> "Lamaran Ditolak"
                        else -> "Lamaran Diproses"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = when (application.status.lowercase()) {
                        "accepted" -> MaterialTheme.colorScheme.onSecondaryContainer
                        "rejected" -> MaterialTheme.colorScheme.onErrorContainer
                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Informasi Lowongan
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { onViewInternship() },
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Informasi Lowongan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = "Lihat Detail",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = internship?.title ?: "Loading...",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (internship != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // PERBAIKAN: Ganti Chip dengan AssistChip
                        AssistChip(
                            onClick = {},
                            label = { Text(internship!!.category) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                        AssistChip(
                            onClick = {},
                            label = { Text(internship!!.location) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Durasi: ${internship!!.duration}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Detail Lamaran
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Detail Lamaran",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                DetailRow(
                    label = "Tanggal Dikirim",
                    value = formatDate(application.appliedAt)
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )

                Text(
                    text = "Motivasi",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = application.motivation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (!application.cvUrl.isNullOrBlank()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AttachFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CV Terlampir",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Catatan untuk status lamaran
        if (application.status == "rejected") {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Lamaran Anda tidak diterima. Jangan berkecil hati, coba lamar lowongan lainnya!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        if (application.status == "accepted") {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Selamat! Lamaran Anda diterima. Perusahaan akan menghubungi Anda untuk tahap selanjutnya.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun formatDate(dateString: String): String {
    return try {
        val parts = dateString.split("T")
        val datePart = parts[0].split("-")
        val year = datePart[0]
        val month = when (datePart[1]) {
            "01" -> "Januari"
            "02" -> "Februari"
            "03" -> "Maret"
            "04" -> "April"
            "05" -> "Mei"
            "06" -> "Juni"
            "07" -> "Juli"
            "08" -> "Agustus"
            "09" -> "September"
            "10" -> "Oktober"
            "11" -> "November"
            "12" -> "Desember"
            else -> datePart[1]
        }
        val day = datePart[2]
        "$day $month $year"
    } catch (e: Exception) {
        dateString.take(10)
    }
}