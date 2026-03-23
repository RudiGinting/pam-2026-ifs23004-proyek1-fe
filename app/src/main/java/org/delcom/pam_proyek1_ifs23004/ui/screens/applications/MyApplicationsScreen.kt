package org.delcom.pam_proyek1_ifs23004.ui.screens.applications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.delcom.pam_proyek1_ifs23004.helper.ConstHelper
import org.delcom.pam_proyek1_ifs23004.helper.RouteHelper
import org.delcom.pam_proyek1_ifs23004.network.internships.data.ResponseApplicationData
import org.delcom.pam_proyek1_ifs23004.ui.components.*
import org.delcom.pam_proyek1_ifs23004.ui.viewmodels.*

@Composable
fun MyApplicationsScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    internshipViewModel: InternshipViewModel
) {
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val uiStateInternship by internshipViewModel.uiState.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var applications by remember { mutableStateOf<List<ResponseApplicationData>>(emptyList()) }
    var authToken by remember { mutableStateOf<String?>(null) }

    fun fetchApplications() {
        val authState = uiStateAuth.auth
        if (authState is AuthUIState.Success) {
            isLoading = true
            authToken = authState.data.authToken
            internshipViewModel.getMyApplications(authToken ?: "")
        }
    }

    LaunchedEffect(Unit) {
        fetchApplications()
    }

    LaunchedEffect(uiStateAuth.auth) {
        val authState = uiStateAuth.auth
        if (authState is AuthUIState.Error) {
            RouteHelper.to(navController, ConstHelper.RouteNames.AuthLogin.path, true)
        }
    }

    LaunchedEffect(uiStateInternship.myApplications) {
        when (val state = uiStateInternship.myApplications) {
            is ApplicationsUIState.Success -> {
                applications = state.data
                isLoading = false
            }
            is ApplicationsUIState.Error -> {
                isLoading = false
            }
            else -> {}
        }
    }

    fun onLogout(token: String) {
        isLoading = true
        authViewModel.logout(token)
    }

    LaunchedEffect(uiStateAuth.authLogout) {
        if (uiStateAuth.authLogout !is AuthLogoutUIState.Loading) {
            RouteHelper.to(navController, ConstHelper.RouteNames.AuthLogin.path, true)
        }
    }

    if (isLoading && applications.isEmpty()) {
        LoadingUI()
        return
    }

    val menuItems = listOf(
        TopAppBarMenuItem(
            text = "Profile",
            icon = Icons.Filled.Person,
            route = ConstHelper.RouteNames.Profile.path
        ),
        TopAppBarMenuItem(
            text = "Logout",
            icon = Icons.AutoMirrored.Filled.Logout,
            route = null,
            onClick = { onLogout(authToken ?: "") }
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBarComponent(
            navController = navController,
            title = "Lamaran Saya",
            showBackButton = false,
            customMenuItems = menuItems
        )

        Box(modifier = Modifier.weight(1f)) {
            if (applications.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Inbox,
                        contentDescription = "Kosong",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Belum ada lamaran yang diajukan.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            RouteHelper.to(navController, ConstHelper.RouteNames.Internships.path)
                        }
                    ) {
                        Text("Cari Lowongan")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = applications,
                        key = { it.id }
                    ) { application ->
                        ApplicationItemUI(
                            application = application,
                            onClick = {
                                RouteHelper.to(
                                    navController,
                                    ConstHelper.RouteNames.ApplicationDetail.path
                                        .replace("{applicationId}", application.id)
                                )
                            }
                        )
                    }
                }
            }
        }
        BottomNavComponent(navController = navController)
    }
}

@Composable
fun ApplicationItemUI(
    application: ResponseApplicationData,
    onClick: () -> Unit
) {
    // NULL SAFETY LENGKAP - PERBAIKAN UTAMA
    val companyName = application.companyName?.takeIf { it.isNotBlank() } ?: "Perusahaan"
    val internshipTitle = application.internshipTitle?.takeIf { it.isNotBlank() } ?: "Lowongan Magang"
    val appliedAt = application.appliedAt ?: ""
    val motivation = application.motivation ?: ""
    val status = application.status ?: "pending"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Baris 1: Nama Perusahaan dan Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = companyName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )

                val statusColor = when (status.lowercase()) {
                    "accepted" -> MaterialTheme.colorScheme.secondaryContainer
                    "rejected" -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.tertiaryContainer
                }
                val statusTextColor = when (status.lowercase()) {
                    "accepted" -> MaterialTheme.colorScheme.onSecondaryContainer
                    "rejected" -> MaterialTheme.colorScheme.onErrorContainer
                    else -> MaterialTheme.colorScheme.onTertiaryContainer
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(statusColor)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (status.lowercase()) {
                            "accepted" -> "Diterima"
                            "rejected" -> "Ditolak"
                            else -> "Diproses"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Baris 2: Judul Lowongan
            Text(
                text = internshipTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Baris 3: Tanggal Dikirim
            Text(
                text = "📅 Dikirim: ${formatDisplayDate(appliedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Baris 4: Motivasi (preview) - PERBAIKAN UTAMA
            val displayMotivation = if (motivation.isNotEmpty()) {
                if (motivation.length > 100) {
                    "${motivation.take(100)}..."
                } else {
                    motivation
                }
            } else {
                "Tidak ada motivasi"
            }

            Text(
                text = displayMotivation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

fun formatDisplayDate(dateString: String): String {
    return try {
        if (dateString.isBlank()) return "-"
        val parts = dateString.split("T")
        val datePart = parts[0].split("-")
        if (datePart.size >= 3) {
            val year = datePart[0]
            val month = when (datePart[1]) {
                "01" -> "Jan"
                "02" -> "Feb"
                "03" -> "Mar"
                "04" -> "Apr"
                "05" -> "Mei"
                "06" -> "Jun"
                "07" -> "Jul"
                "08" -> "Agu"
                "09" -> "Sep"
                "10" -> "Okt"
                "11" -> "Nov"
                "12" -> "Des"
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