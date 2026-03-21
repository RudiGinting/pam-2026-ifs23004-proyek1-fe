package org.delcom.pam_proyek1_ifs23004.ui.screens.internships

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import org.delcom.pam_proyek1_ifs23004.R
import org.delcom.pam_proyek1_ifs23004.helper.ConstHelper
import org.delcom.pam_proyek1_ifs23004.helper.RouteHelper
import org.delcom.pam_proyek1_ifs23004.helper.ToolsHelper
import org.delcom.pam_proyek1_ifs23004.network.internships.data.CategoryEnum
import org.delcom.pam_proyek1_ifs23004.network.internships.data.LocationEnum
import org.delcom.pam_proyek1_ifs23004.network.internships.data.ResponseInternshipData
import org.delcom.pam_proyek1_ifs23004.ui.components.*
import org.delcom.pam_proyek1_ifs23004.ui.theme.DelcomTheme
import org.delcom.pam_proyek1_ifs23004.ui.viewmodels.*

@Composable
fun InternshipsScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    internshipViewModel: InternshipViewModel
) {
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val uiStateInternship by internshipViewModel.uiState.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedLocation by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    var internships by remember { mutableStateOf<List<ResponseInternshipData>>(emptyList()) }
    var authToken by remember { mutableStateOf<String?>(null) }

    fun fetchInternshipsData() {
        val authState = uiStateAuth.auth
        if (authState is AuthUIState.Success) {
            isLoading = true
            authToken = authState.data.authToken
            internshipViewModel.resetAndGetAllInternships(
                authToken ?: "",
                searchQuery.text,
                selectedCategory,
                selectedLocation
            )
        }
    }

    LaunchedEffect(uiStateAuth.auth) {
        val authState = uiStateAuth.auth
        if (authState is AuthUIState.Success) {
            fetchInternshipsData()
        } else if (authState is AuthUIState.Error) {
            RouteHelper.to(navController, ConstHelper.RouteNames.AuthLogin.path, true)
        }
    }

    val shouldLoadMore = remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItem >= (totalItems - 2) && totalItems > 0
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && uiStateInternship.internships !is InternshipsUIState.Loading) {
            internshipViewModel.getAllInternships(
                authToken ?: "",
                searchQuery.text,
                selectedCategory,
                selectedLocation
            )
        }
    }

    LaunchedEffect(uiStateInternship.internships) {
        if (uiStateInternship.internships !is InternshipsUIState.Loading) {
            isLoading = false
            internships = if (uiStateInternship.internships is InternshipsUIState.Success) {
                (uiStateInternship.internships as InternshipsUIState.Success).data
            } else {
                emptyList()
            }
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

    if (isLoading && internships.isEmpty()) {
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
            title = "Lowongan Magang",
            showBackButton = false,
            customMenuItems = menuItems,
            withSearch = true,
            searchQuery = searchQuery,
            onSearchQueryChange = { query -> searchQuery = query },
            onSearchAction = { fetchInternshipsData() }
        )

        // Filter Area - Menggunakan Row dengan horizontalScroll
        Column(modifier = Modifier.fillMaxWidth()) {
            // Filter Kategori
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Filter "Semua"
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = {
                        selectedCategory = null
                        fetchInternshipsData()
                    },
                    label = { Text("Semua") },
                    shape = RoundedCornerShape(50)
                )

                // Filter berdasarkan kategori
                CategoryEnum.entries.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category.fullName,
                        onClick = {
                            selectedCategory = category.fullName
                            fetchInternshipsData()
                        },
                        label = { Text(category.shortName) },
                        shape = RoundedCornerShape(50)
                    )
                }
            }

            // Filter Lokasi
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Filter "Semua"
                FilterChip(
                    selected = selectedLocation == null,
                    onClick = {
                        selectedLocation = null
                        fetchInternshipsData()
                    },
                    label = { Text("Semua") },
                    shape = RoundedCornerShape(50)
                )

                // Filter berdasarkan lokasi
                LocationEnum.entries.forEach { location ->
                    FilterChip(
                        selected = selectedLocation == location.value,
                        onClick = {
                            selectedLocation = location.value
                            fetchInternshipsData()
                        },
                        label = { Text(location.value) },
                        shape = RoundedCornerShape(50)
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            if (internships.isEmpty()) {
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
                        text = "Belum ada lowongan magang.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = internships,
                        key = { it.id }
                    ) { internship ->
                        InternshipItemUI(
                            internship = internship,
                            onClick = {
                                RouteHelper.to(
                                    navController,
                                    ConstHelper.RouteNames.InternshipsDetail.path
                                        .replace("{internshipId}", internship.id)
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
fun InternshipItemUI(
    internship: ResponseInternshipData,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ToolsHelper.getInternshipImage(internship.id, internship.updatedAt),
                contentDescription = internship.title,
                placeholder = painterResource(R.drawable.img_placeholder),
                error = painterResource(R.drawable.img_placeholder),
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = internship.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${internship.category} • ${internship.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Durasi: ${internship.duration}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Deadline: ${internship.deadline}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewInternshipItemUI() {
    DelcomTheme {
        InternshipItemUI(
            internship = ResponseInternshipData(
                id = "1",
                title = "Backend Developer Intern",
                description = "",
                category = "IT",
                location = "Remote",
                duration = "3 bulan",
                requirement = "",
                deadline = "2026-05-30"
            ),
            onClick = {}
        )
    }
}