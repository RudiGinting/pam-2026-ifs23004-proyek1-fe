package org.delcom.pam_proyek1_ifs23004.ui.components

import android.content.res.Configuration
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.delcom.pam_proyek1_ifs23004.helper.ConstHelper
import org.delcom.pam_proyek1_ifs23004.helper.RouteHelper
import org.delcom.pam_proyek1_ifs23004.ui.theme.DelcomTheme

data class TopAppBarMenuItem(
    val text: String,
    val icon: ImageVector,
    val route: String? = null,
    val onClick: (() -> Unit)? = null,
    val isDestructive: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarComponent(
    navController: NavHostController,
    title: String = "Home",
    showBackButton: Boolean = true,
    showMenu: Boolean = true,
    customMenuItems: List<TopAppBarMenuItem>? = null,
    onBackClick: (() -> Unit)? = null,
    elevation: Int = 8, // Sedikit lebih tebal bayangannya
    withSearch: Boolean = false,
    searchQuery: TextFieldValue = TextFieldValue(""),
    onSearchQueryChange: (TextFieldValue) -> Unit = {},
    onSearchAction: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    val queryFocusRequester = remember { FocusRequester() }

    fun setExpandState(state: Boolean) {
        expanded = state
    }

    fun setSearchState(state: Boolean) {
        isSearching = state
    }


    // Menu items default
    val defaultMenuItems = listOf(
        TopAppBarMenuItem(
            text = "Profile",
            icon = Icons.Filled.Person,
            route = ConstHelper.RouteNames.Profile.path
        ),
    )

    val menuItems = customMenuItems ?: defaultMenuItems

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = elevation.dp,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp), // Melengkung modern
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            ),
//        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        color = MaterialTheme.colorScheme.primary, // PERBAIKAN: Background Biru Tua Utama
        tonalElevation = elevation.dp
    ) {
        TopAppBar(
            modifier = Modifier.padding(horizontal = 4.dp), // Sedikit jarak dari pinggir layar
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    if (showBackButton && !isSearching) {
                        // Back button (Tombol Kembali)
                        Card(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape), // PERBAIKAN: Bentuk bulat penuh agar elegan
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.2f), // Semi transparan di atas biru
                            ),
                            onClick = {
                                onBackClick?.invoke() ?: RouteHelper.back(navController)
                            }
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary // Ikon putih
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))
                    }

                    if (isSearching) {
                        // Area Pencarian
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.2f)), // Background search bar
                            contentAlignment = Alignment.CenterStart
                        ) {
                            TextField(
                                value = searchQuery,
                                onValueChange = { onSearchQueryChange(it) },
                                placeholder = {
                                    Text(
                                        "Cari kegiatan...",
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(queryFocusRequester),

                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Search
                                ),

                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        onSearchAction()
                                    }
                                ),

                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    errorContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent, // Hilangkan garis bawah
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    cursorColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                textStyle = MaterialTheme.typography.bodyLarge
                            )
                        }

                        SideEffect {
                            queryFocusRequester.requestFocus()
                        }
                    } else {
                        // Title
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold // PERBAIKAN: Judul lebih tebal
                            ),
                            color = MaterialTheme.colorScheme.onPrimary // Teks Putih
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent, // Agar mengikuti background Surface di atasnya (Primary)
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimary // Ikon-ikon menu berwarna putih
            ),
            actions = {
                if (isSearching) {
                    // Close search button
                    IconButton(
                        onClick = {
                            setSearchState(false)
                            onSearchQueryChange(TextFieldValue(""))
                            onSearchAction()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Tutup pencarian",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                } else {
                    // Search button
                    if (withSearch) {
                        IconButton(
                            onClick = {
                                setSearchState(true)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Cari",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    // Menu Button (Titik 3)
                    if (showMenu) {
                        Box {
                            IconButton(
                                onClick = { expanded = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "Menu",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }

                            // Dropdown menu popup
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = {
                                    setExpandState(false)
                                },
                                containerColor = MaterialTheme.colorScheme.surface, // Balik ke putih/hitam
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.padding(8.dp)
                            ) {
                                // Menu items
                                menuItems.forEachIndexed { index, item ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Icon(
                                                    imageVector = item.icon,
                                                    contentDescription = item.text,
                                                    modifier = Modifier.size(20.dp),
                                                    tint = if (item.isDestructive)
                                                        MaterialTheme.colorScheme.error
                                                    else
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                )

                                                Text(
                                                    text = item.text,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (item.isDestructive)
                                                        MaterialTheme.colorScheme.error
                                                    else
                                                        MaterialTheme.colorScheme.onSurface,
                                                    fontWeight = if (item.isDestructive) FontWeight.Bold else FontWeight.Medium
                                                )
                                            }
                                        },
                                        onClick = {
                                            setExpandState(false)
                                            if (item.route != null) {
                                                RouteHelper.to(
                                                    navController,
                                                    item.route
                                                )
                                            }
                                            item.onClick?.invoke()
                                        },
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )

                                    // Divider sebelum item terakhir jika destructive
                                    if (index == menuItems.size - 2 && menuItems.last().isDestructive) {
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}


// Preview functions
@Preview(showBackground = true, name = "Default - Light Mode")
@Composable
fun PreviewTopAppBarWithBackComponent() {
    DelcomTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                TopAppBarComponent(
                    navController = NavHostController(LocalContext.current),
                    title = "Profile"
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    name = "Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewTopAppBarWithBackComponentDark() {
    DelcomTheme(darkTheme = true) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                TopAppBarComponent(
                    navController = NavHostController(LocalContext.current),
                    title = "Profile Settings"
                )
            }
        }
    }
}