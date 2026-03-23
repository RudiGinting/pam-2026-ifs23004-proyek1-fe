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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.delcom.pam_proyek1_ifs23004.helper.ConstHelper
import org.delcom.pam_proyek1_ifs23004.helper.RouteHelper
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
                    internshipViewModel.getInternshipById(foundApplication.internshipId)
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
                onClick = { showDeleteDialog = true },
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
                    onClick = { onDelete() },
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
    val status = application.status ?: "pending"
    val statusLower = status.lowercase()

    val companyName = internship?.companyName ?: "Perusahaan"
    val internshipTitle = internship?.title ?: "Loading..."
    val category = internship?.category ?: "-"
    val location = internship?.location ?: "-"
    val duration = internship?.duration ?: "-"

    val appliedAt = application.appliedAt ?: ""
    val motivation = application.motivation ?: ""
    val cvUrl = application.cvUrl

    // Warna gradien untuk header
    val gradientColors = when (statusLower) {
        "accepted" -> listOf(Color(0xFF10B981), Color(0xFF059669))
        "rejected" -> listOf(Color(0xFFEF4444), Color(0xFFDC2626))
        else -> listOf(Color(0xFF3B82F6), Color(0xFF2563EB))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header dengan Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = gradientColors,
                        startY = 0f,
                        endY = 500f
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon Status
                Surface(
                    modifier = Modifier.size(70.dp),
                    shape = RoundedCornerShape(35.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (statusLower) {
                                "accepted" -> Icons.Filled.CheckCircle
                                "rejected" -> Icons.Filled.Cancel
                                else -> Icons.Filled.Schedule
                            },
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = when (statusLower) {
                                "accepted" -> Color(0xFF10B981)
                                "rejected" -> Color(0xFFEF4444)
                                else -> Color(0xFF3B82F6)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = when (statusLower) {
                        "accepted" -> "Lamaran Diterima!"
                        "rejected" -> "Lamaran Ditolak"
                        else -> "Lamaran Diproses"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Informasi Lowongan
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { onViewInternship() },
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
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
                    text = internshipTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = companyName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text(category) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text(location) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Durasi: $duration",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Detail Lamaran
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Detail Lamaran",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                DetailRow(
                    label = "Tanggal Dikirim",
                    value = formatDate(appliedAt)
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

                val displayMotivation = if (motivation.isNotEmpty()) {
                    motivation
                } else {
                    "Tidak ada motivasi"
                }

                Text(
                    text = displayMotivation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (!cvUrl.isNullOrBlank()) {
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
        when (statusLower) {
            "rejected" -> {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
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
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(28.dp)
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
            "accepted" -> {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(28.dp)
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
        if (dateString.isBlank()) return "-"
        val parts = dateString.split("T")
        val datePart = parts[0].split("-")
        if (datePart.size >= 3) {
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
        } else {
            dateString.take(10)
        }
    } catch (e: Exception) {
        dateString.take(10)
    }
}