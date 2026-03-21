package org.delcom.pam_proyek1_ifs23004.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.delcom.pam_proyek1_ifs23004.R
import org.delcom.pam_proyek1_ifs23004.helper.ConstHelper
import org.delcom.pam_proyek1_ifs23004.helper.RouteHelper
import org.delcom.pam_proyek1_ifs23004.helper.ToolsHelper
import org.delcom.pam_proyek1_ifs23004.network.internships.data.ResponseUserData
import org.delcom.pam_proyek1_ifs23004.ui.components.BottomNavComponent
import org.delcom.pam_proyek1_ifs23004.ui.components.LoadingUI
import org.delcom.pam_proyek1_ifs23004.ui.components.TopAppBarComponent
import org.delcom.pam_proyek1_ifs23004.ui.components.TopAppBarMenuItem
import org.delcom.pam_proyek1_ifs23004.ui.theme.DelcomTheme
import org.delcom.pam_proyek1_ifs23004.ui.viewmodels.*
import java.io.File
import java.io.FileOutputStream

@Composable
fun ProfileScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    internshipViewModel: InternshipViewModel
) {
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val uiStateInternship by internshipViewModel.uiState.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var profile by remember { mutableStateOf<ResponseUserData?>(null) }
    var authToken by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showEditPasswordDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val filePart = getMultipartFromUri(context, it)
            if (filePart != null && authToken != null) {
                internshipViewModel.putUserMePhoto(authToken!!, filePart)
            } else {
                Toast.makeText(context, "Gagal memproses gambar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        isLoading = true

        if (uiStateAuth.auth !is AuthUIState.Success) {
            RouteHelper.to(navController, ConstHelper.RouteNames.Home.path, true)
            return@LaunchedEffect
        }

        authToken = (uiStateAuth.auth as AuthUIState.Success).data.authToken

        if (uiStateInternship.profile is ProfileUIState.Success) {
            profile = (uiStateInternship.profile as ProfileUIState.Success).data
            isLoading = false
            return@LaunchedEffect
        }

        internshipViewModel.getProfile(authToken ?: "")
    }

    LaunchedEffect(uiStateInternship.profile) {
        if (uiStateInternship.profile !is ProfileUIState.Loading) {
            isLoading = false
            if (uiStateInternship.profile is ProfileUIState.Success) {
                profile = (uiStateInternship.profile as ProfileUIState.Success).data
            } else {
                RouteHelper.to(navController, ConstHelper.RouteNames.Home.path, true)
            }
        }
    }

    LaunchedEffect(
        uiStateInternship.profileChange,
        uiStateInternship.profileChangePassword,
        uiStateInternship.profileChangePhoto
    ) {
        val states = listOf(
            uiStateInternship.profileChange,
            uiStateInternship.profileChangePassword,
            uiStateInternship.profileChangePhoto
        )

        states.forEach { state ->
            if (state is InternshipActionUIState.Success) {
                Toast.makeText(context, "Berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                internshipViewModel.getProfile(authToken ?: "")
            } else if (state is InternshipActionUIState.Error) {
                Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_SHORT).show()
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

    if (isLoading || profile == null) {
        LoadingUI()
        return
    }

    val menuItems = listOf(
        TopAppBarMenuItem(
            text = "Logout",
            icon = Icons.AutoMirrored.Filled.Logout,
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
            title = "Profile",
            showBackButton = false,
            customMenuItems = menuItems,
            elevation = 0
        )

        Box(modifier = Modifier.weight(1f)) {
            ProfileUI(
                profile = profile!!,
                onEditPhotoClick = {
                    photoPickerLauncher.launch(
                        androidx.activity.result.PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                onEditProfileClick = { showEditProfileDialog = true },
                onEditPasswordClick = { showEditPasswordDialog = true }
            )
        }
        BottomNavComponent(navController = navController)
    }

    // DIALOG EDIT PROFIL
    if (showEditProfileDialog) {
        var inputName by remember { mutableStateOf(profile!!.name) }
        var inputUsername by remember { mutableStateOf(profile!!.username) }
        var inputAbout by remember { mutableStateOf(profile!!.about ?: "") }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profil") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text("Nama Lengkap") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputUsername,
                        onValueChange = { inputUsername = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputAbout,
                        onValueChange = { inputAbout = it },
                        label = { Text("Tentang Saya / Bio") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        internshipViewModel.putUserMe(
                            authToken ?: "",
                            inputName,
                            inputUsername,
                            inputAbout
                        )
                        showEditProfileDialog = false
                        isLoading = true
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // DIALOG UBAH KATA SANDI
    if (showEditPasswordDialog) {
        var oldPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showEditPasswordDialog = false },
            title = { Text("Ubah Kata Sandi") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text("Sandi Lama") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Sandi Baru") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        internshipViewModel.putUserMePassword(
                            authToken ?: "",
                            oldPassword,
                            newPassword
                        )
                        showEditPasswordDialog = false
                        isLoading = true
                    }
                ) {
                    Text("Ubah")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditPasswordDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun ProfileUI(
    profile: ResponseUserData,
    onEditPhotoClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onEditPasswordClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // HEADER MELENGKUNG & FOTO PROFIL
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )

            Box(
                modifier = Modifier
                    .padding(top = 90.dp)
                    .size(140.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                AsyncImage(
                    model = ToolsHelper.getUserImage(profile.id) + "?time=${System.currentTimeMillis()}",
                    contentDescription = "Photo Profil",
                    placeholder = painterResource(R.drawable.img_placeholder),
                    error = painterResource(R.drawable.img_placeholder),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        .shadow(8.dp, CircleShape)
                        .clickable { onEditPhotoClick() }
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .offset(x = (-4).dp, y = (-4).dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                        .clickable { onEditPhotoClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Ganti Foto",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Text(
            text = profile.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "@${profile.username}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 4.dp)
        )

        if (!profile.about.isNullOrBlank()) {
            Text(
                text = "\"${profile.about}\"",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp, start = 32.dp, end = 32.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEditProfileClick() }
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Edit Profil",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Edit Informasi Profil",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEditPasswordClick() }
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Ubah Sandi",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Ubah Kata Sandi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

fun getMultipartFromUri(context: Context, uri: Uri): MultipartBody.Part? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("profile_", ".jpg", context.cacheDir)
        val outputStream = FileOutputStream(tempFile)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()

        val reqFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
        MultipartBody.Part.createFormData("file", tempFile.name, reqFile)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun PreviewProfileUI() {
    DelcomTheme {
        ProfileUI(
            profile = ResponseUserData(
                id = "",
                name = "Daniel L. Tobing",
                username = "ifs23004",
                about = "Mahasiswa S1 Informatika Institut Teknologi Del",
                createdAt = "",
                updatedAt = ""
            ),
            onEditPhotoClick = {},
            onEditProfileClick = {},
            onEditPasswordClick = {}
        )
    }
}