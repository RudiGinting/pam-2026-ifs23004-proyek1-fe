package org.delcom.pam_proyek1_ifs23004.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.delcom.pam_proyek1_ifs23004.helper.ConstHelper
import org.delcom.pam_proyek1_ifs23004.helper.RouteHelper
import org.delcom.pam_proyek1_ifs23004.ui.components.BottomNavComponent
import org.delcom.pam_proyek1_ifs23004.ui.components.LoadingUI
import org.delcom.pam_proyek1_ifs23004.ui.components.StatusCard
import org.delcom.pam_proyek1_ifs23004.ui.components.TopAppBarComponent
import org.delcom.pam_proyek1_ifs23004.ui.components.TopAppBarMenuItem
import org.delcom.pam_proyek1_ifs23004.ui.theme.DelcomTheme
import org.delcom.pam_proyek1_ifs23004.ui.viewmodels.*

// ==========================================
// DATA CLASS UNTUK STATISTIK
// ==========================================
data class ResponseInternshipStatsData(
    val total: Long = 0,
    val pending: Long = 0,
    val accepted: Long = 0,
    val rejected: Long = 0
)

@Composable
fun HomeScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    internshipViewModel: InternshipViewModel
) {
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val uiStateInternship by internshipViewModel.uiState.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var isFreshToken by remember { mutableStateOf(false) }
    var authToken by remember { mutableStateOf<String?>(null) }

    val statsData = remember { mutableStateOf(ResponseInternshipStatsData(0, 0, 0, 0)) }

    LaunchedEffect(Unit) {
        if (isLoading) return@LaunchedEffect

        isLoading = true
        isFreshToken = true
        uiStateAuth.authLogout = AuthLogoutUIState.Loading
        authViewModel.loadTokenFromPreferences()
    }

    LaunchedEffect(authToken) {
        authToken?.let {
            internshipViewModel.getProfile(it)
            internshipViewModel.getMyApplications(it, 1, 100)
        }
    }

    LaunchedEffect(uiStateInternship.myApplications) {
        when (val state = uiStateInternship.myApplications) {
            is ApplicationsUIState.Success -> {
                val applications = state.data
                val total = applications.size.toLong()           // PERBAIKAN: Int -> Long
                val pending = applications.count { it.status == "pending" }.toLong()  // PERBAIKAN: Int -> Long
                val accepted = applications.count { it.status == "accepted" }.toLong() // PERBAIKAN: Int -> Long
                val rejected = applications.count { it.status == "rejected" }.toLong() // PERBAIKAN: Int -> Long
                statsData.value = ResponseInternshipStatsData(total, pending, accepted, rejected)
            }
            else -> {}
        }
    }

    fun onLogout(token: String) {
        isLoading = true
        authViewModel.logout(token)
    }

    LaunchedEffect(uiStateAuth.auth) {
        if (!isLoading) {
            return@LaunchedEffect
        }

        if (uiStateAuth.auth !is AuthUIState.Loading) {
            if (uiStateAuth.auth is AuthUIState.Success) {
                if (isFreshToken) {
                    val dataToken = (uiStateAuth.auth as AuthUIState.Success).data
                    authViewModel.refreshToken(dataToken.authToken, dataToken.refreshToken)
                    isFreshToken = false
                } else if (uiStateAuth.authRefreshToken is AuthActionUIState.Success) {
                    val newToken = (uiStateAuth.auth as AuthUIState.Success).data.authToken
                    if (authToken != newToken) {
                        authToken = (uiStateAuth.auth as AuthUIState.Success).data.authToken
                    }
                    isLoading = false
                }
            } else {
                onLogout("")
            }
        }
    }

    LaunchedEffect(uiStateAuth.authLogout) {
        if (uiStateAuth.authLogout !is AuthLogoutUIState.Loading) {
            RouteHelper.to(navController, ConstHelper.RouteNames.AuthLogin.path, true)
        }
    }

    if (isLoading || authToken == null || isFreshToken) {
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
            icon = Icons.AutoMirrored.Filled.Logout,  // PERBAIKAN: Icon logout yang benar
            route = null,
            onClick = { onLogout(authToken ?: "") }
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBarComponent(
            navController = navController,
            title = "Home",
            showBackButton = false,
            customMenuItems = menuItems
        )
        Box(modifier = Modifier.weight(1f)) {
            HomeUI(
                statsState = statsData.value,
                profileState = uiStateInternship.profile,
                onNavigateToInternships = {
                    RouteHelper.to(navController, ConstHelper.RouteNames.Internships.path)
                },
                onNavigateToMyApplications = {
                    RouteHelper.to(navController, ConstHelper.RouteNames.MyApplications.path)
                }
            )
        }
        BottomNavComponent(navController = navController)
    }
}

@Composable
fun HomeUI(
    statsState: ResponseInternshipStatsData,
    profileState: ProfileUIState,
    onNavigateToInternships: () -> Unit,
    onNavigateToMyApplications: () -> Unit
) {
    val scrollState = rememberScrollState()

    val userName = when (profileState) {
        is ProfileUIState.Success -> profileState.data.name
        else -> "Pengguna"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Halo, $userName! 👋",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Temukan lowongan magang terbaik untukmu.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // STATISTIK LAMARAN
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📊 Statistik Lamaran Saya",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatusCard(
                        title = "Total Lamaran",
                        value = statsState.total.toString(),
                        icon = Icons.Filled.Work,
                        modifier = Modifier.weight(1f)
                    )
                    StatusCard(
                        title = "Pending",
                        value = statsState.pending.toString(),
                        icon = Icons.Default.Schedule,
                        modifier = Modifier.weight(1f)
                    )
                    StatusCard(
                        title = "Diterima",
                        value = statsState.accepted.toString(),
                        icon = Icons.Default.CheckCircle,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Aksi Cepat",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onNavigateToInternships,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.Work, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cari Lowongan", fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = onNavigateToMyApplications,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text("Lihat Lamaran", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun PreviewHomeUI() {
    DelcomTheme {
        HomeUI(
            statsState = ResponseInternshipStatsData(10, 5, 3, 2),
            profileState = ProfileUIState.Loading,
            onNavigateToInternships = {},
            onNavigateToMyApplications = {}
        )
    }
}