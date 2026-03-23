package org.delcom.pam_proyek1_ifs23004.ui.screens.internships

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import org.delcom.pam_proyek1_ifs23004.R
import org.delcom.pam_proyek1_ifs23004.helper.ConstHelper
import org.delcom.pam_proyek1_ifs23004.helper.RouteHelper
import org.delcom.pam_proyek1_ifs23004.helper.ToolsHelper
import org.delcom.pam_proyek1_ifs23004.network.internships.data.ResponseInternshipData
import org.delcom.pam_proyek1_ifs23004.ui.components.*
import org.delcom.pam_proyek1_ifs23004.ui.viewmodels.*

@Composable
fun InternshipDetailScreen(
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
    var showApplyDialog by remember { mutableStateOf(false) }
    var motivation by remember { mutableStateOf("") }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        isLoading = true

        if (uiStateAuth.auth !is AuthUIState.Success) {
            RouteHelper.to(navController, ConstHelper.RouteNames.Home.path, true)
            return@LaunchedEffect
        }

        authToken = (uiStateAuth.auth as AuthUIState.Success).data.authToken
        internshipViewModel.getInternshipById(internshipId)
    }

    LaunchedEffect(uiStateInternship.internship) {
        when (val state = uiStateInternship.internship) {
            is InternshipUIState.Success -> {
                internship = state.data
                isLoading = false
            }
            is InternshipUIState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                RouteHelper.back(navController)
                isLoading = false
            }
            else -> {}
        }
    }

    fun onApply() {
        if (motivation.isBlank()) {
            Toast.makeText(context, "Motivasi tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }
        isLoading = true
        internshipViewModel.postApplication(authToken!!, internshipId, motivation)
    }

    LaunchedEffect(uiStateInternship.applicationAdd) {
        when (val state = uiStateInternship.applicationAdd) {
            is InternshipActionUIState.Success -> {
                Toast.makeText(context, "Lamaran berhasil dikirim!", Toast.LENGTH_SHORT).show()
                showApplyDialog = false
                motivation = ""
                isLoading = false
            }
            is InternshipActionUIState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                isLoading = false
            }
            else -> {}
        }
    }

    if (isLoading || internship == null) {
        LoadingUI()
        return
    }

    val safeInternship = internship!!

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBarComponent(
            navController = navController,
            title = "Detail Lowongan",
            showBackButton = true,
        )
        Box(modifier = Modifier.weight(1f)) {
            InternshipDetailUI(
                internship = safeInternship,
                onApplyClick = { showApplyDialog = true }
            )
        }
        BottomNavComponent(navController = navController)
    }

    // Dialog Apply
    if (showApplyDialog) {
        AlertDialog(
            onDismissRequest = { showApplyDialog = false },
            title = { Text("Lamaran Magang") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = safeInternship.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = motivation,
                        onValueChange = { motivation = it },
                        label = { Text("Motivasi Melamar") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        maxLines = 6,
                        placeholder = {
                            Text("Jelaskan mengapa Anda tertarik dengan posisi ini...")
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { onApply() },
                    enabled = motivation.isNotBlank()
                ) {
                    Text("Kirim Lamaran")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApplyDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun InternshipDetailUI(
    internship: ResponseInternshipData,
    onApplyClick: () -> Unit
) {
    // Null safety untuk semua field
    val title = internship.title ?: "Tanpa Judul"
    val status = internship.status ?: "Open"
    val companyName = internship.companyName ?: "Perusahaan"
    val companyEmail = internship.companyEmail ?: "-"
    val submissionDate = internship.submissionDate ?: "-"
    val category = internship.category ?: "-"
    val location = internship.location ?: "-"
    val duration = internship.duration ?: "-"
    val requirement = internship.requirement ?: "-"
    val description = internship.description ?: "-"
    val deadline = internship.deadline ?: "-"
    val applicantsCount = internship.applicantsCount ?: 0
    val benefit = internship.benefit

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Cover
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            AsyncImage(
                model = ToolsHelper.getInternshipImage(internship.id, internship.updatedAt),
                contentDescription = title,
                placeholder = painterResource(R.drawable.img_placeholder),
                error = painterResource(R.drawable.img_placeholder),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Judul
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Status Chip
        val statusColor = when (status.lowercase()) {
            "closed" -> MaterialTheme.colorScheme.errorContainer
            else -> MaterialTheme.colorScheme.secondaryContainer
        }
        val statusTextColor = when (status.lowercase()) {
            "closed" -> MaterialTheme.colorScheme.onErrorContainer
            else -> MaterialTheme.colorScheme.onSecondaryContainer
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(statusColor)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = status,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = statusTextColor
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Informasi Perusahaan
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🏢 Informasi Perusahaan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = companyName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = companyEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "📅 Tanggal Pengajuan: $submissionDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "👥 $applicantsCount Pelamar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Detail Lowongan
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                DetailSection(
                    icon = Icons.Filled.Description,
                    title = "Deskripsi Pekerjaan",
                    content = description
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                DetailSection(
                    icon = Icons.Filled.CheckCircle,
                    title = "Kualifikasi",
                    content = requirement
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                DetailSection(
                    icon = Icons.Filled.Schedule,
                    title = "Durasi",
                    content = duration
                )

                if (!benefit.isNullOrBlank()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    DetailSection(
                        icon = Icons.Filled.Star,
                        title = "Benefit",
                        content = benefit!!
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tombol Apply (hanya jika status Open)
        if (status == "Open") {
            Button(
                onClick = onApplyClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Filled.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lamar Sekarang", fontWeight = FontWeight.Bold)
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Cancel,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lowongan Sudah Ditutup",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun DetailSection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}