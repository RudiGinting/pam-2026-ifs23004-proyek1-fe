package org.delcom.pam_proyek1_ifs23004.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.delcom.pam_proyek1_ifs23004.helper.ConstHelper
import org.delcom.pam_proyek1_ifs23004.ui.components.CustomSnackbar
import org.delcom.pam_proyek1_ifs23004.ui.screens.HomeScreen
import org.delcom.pam_proyek1_ifs23004.ui.screens.ProfileScreen
import org.delcom.pam_proyek1_ifs23004.ui.screens.applications.ApplicationDetailScreen
import org.delcom.pam_proyek1_ifs23004.ui.screens.applications.MyApplicationsScreen
import org.delcom.pam_proyek1_ifs23004.ui.screens.auth.AuthLoginScreen
import org.delcom.pam_proyek1_ifs23004.ui.screens.auth.AuthRegisterScreen
import org.delcom.pam_proyek1_ifs23004.ui.screens.internships.InternshipAddScreen
import org.delcom.pam_proyek1_ifs23004.ui.screens.internships.InternshipDetailScreen
import org.delcom.pam_proyek1_ifs23004.ui.screens.internships.InternshipEditScreen
import org.delcom.pam_proyek1_ifs23004.ui.screens.internships.InternshipsScreen
import org.delcom.pam_proyek1_ifs23004.ui.viewmodels.AuthViewModel
import org.delcom.pam_proyek1_ifs23004.ui.viewmodels.InternshipViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun UIApp(
    navController: NavHostController = rememberNavController(),
    internshipViewModel: InternshipViewModel,
    authViewModel: AuthViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) { snackbarData ->
            CustomSnackbar(snackbarData, onDismiss = { snackbarHostState.currentSnackbarData?.dismiss() })
        } },
    ) { _ ->
        NavHost(
            navController = navController,
            startDestination = ConstHelper.RouteNames.Home.path,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F8FA))
        ) {
            // ==========================================
            // Auth Screens
            // ==========================================
            composable(route = ConstHelper.RouteNames.AuthLogin.path) { _ ->
                AuthLoginScreen(
                    navController = navController,
                    snackbarHost = snackbarHostState,
                    authViewModel = authViewModel,
                )
            }

            composable(route = ConstHelper.RouteNames.AuthRegister.path) { _ ->
                AuthRegisterScreen(
                    navController = navController,
                    snackbarHost = snackbarHostState,
                    authViewModel = authViewModel,
                )
            }

            // ==========================================
            // Main Screens
            // ==========================================
            composable(route = ConstHelper.RouteNames.Home.path) { _ ->
                HomeScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    internshipViewModel = internshipViewModel
                )
            }

            composable(route = ConstHelper.RouteNames.Profile.path) { _ ->
                ProfileScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    internshipViewModel = internshipViewModel
                )
            }

            // ==========================================
            // Internships Screens (Lowongan Magang)
            // ==========================================
            composable(route = ConstHelper.RouteNames.Internships.path) { _ ->
                InternshipsScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    internshipViewModel = internshipViewModel
                )
            }

            composable(route = ConstHelper.RouteNames.InternshipsAdd.path) { _ ->
                InternshipAddScreen(
                    navController = navController,
                    snackbarHost = snackbarHostState,
                    authViewModel = authViewModel,
                    internshipViewModel = internshipViewModel
                )
            }

            composable(
                route = ConstHelper.RouteNames.InternshipsDetail.path,
                arguments = listOf(
                    navArgument("internshipId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val internshipId = backStackEntry.arguments?.getString("internshipId") ?: ""
                InternshipDetailScreen(
                    navController = navController,
                    snackbarHost = snackbarHostState,
                    authViewModel = authViewModel,
                    internshipViewModel = internshipViewModel,
                    internshipId = internshipId
                )
            }

            composable(
                route = ConstHelper.RouteNames.InternshipsEdit.path,
                arguments = listOf(
                    navArgument("internshipId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val internshipId = backStackEntry.arguments?.getString("internshipId") ?: ""
                InternshipEditScreen(
                    navController = navController,
                    snackbarHost = snackbarHostState,
                    authViewModel = authViewModel,
                    internshipViewModel = internshipViewModel,
                    internshipId = internshipId
                )
            }

            // ==========================================
            // Applications Screens (Lamaran)
            // ==========================================
            composable(route = ConstHelper.RouteNames.MyApplications.path) { _ ->
                MyApplicationsScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    internshipViewModel = internshipViewModel
                )
            }

            // ==========================================
            // Application Detail Screen (TAMBAHKAN INI)
            // ==========================================
            composable(
                route = ConstHelper.RouteNames.ApplicationDetail.path,
                arguments = listOf(
                    navArgument("applicationId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val applicationId = backStackEntry.arguments?.getString("applicationId") ?: ""
                ApplicationDetailScreen(
                    navController = navController,
                    snackbarHost = snackbarHostState,
                    authViewModel = authViewModel,
                    internshipViewModel = internshipViewModel,
                    applicationId = applicationId
                )
            }
        }
    }
}