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
import androidx.compose.ui.graphics.Brush  // TAMBAHKAN IMPORT INI
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
        internshipViewModel.getInternshipById(authToken!!, internshipId)
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
                internship = internship!!,
                onApplyClick = { showApplyDialog = true }  // PERBAIKAN: hanya set showApplyDialog = true
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
                        text = internship!!.title,
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
                    onClick = { onApply() },  // PERBAIKAN: panggil onApply() di dalam lambda
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
                contentDescription = internship.title,
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
                        Brush.verticalGradient(  // PERBAIKAN: Brush sudah diimport
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Judul dan Info
        Text(
            text = internship.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // PERBAIKAN: Ganti Chip dengan AssistChip
            AssistChip(
                onClick = {},
                label = { Text(internship.category) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            AssistChip(
                onClick = {},
                label = { Text(internship.location) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Durasi: ${internship.duration} • Deadline: ${internship.deadline}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Detail Card
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
                    content = internship.description
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                DetailSection(
                    icon = Icons.Filled.CheckCircle,
                    title = "Kualifikasi",
                    content = internship.requirement
                )

                if (!internship.benefit.isNullOrBlank()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    DetailSection(
                        icon = Icons.Filled.Star,
                        title = "Benefit",
                        content = internship.benefit!!
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tombol Apply
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