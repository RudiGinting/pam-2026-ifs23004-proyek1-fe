package org.delcom.pam_proyek1_ifs23004.ui.components

import android.content.res.Configuration
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.delcom.pam_proyek1_ifs23004.helper.ConstHelper
import org.delcom.pam_proyek1_ifs23004.helper.RouteHelper
import org.delcom.pam_proyek1_ifs23004.ui.theme.DelcomTheme

sealed class MenuBottomNav(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val iconActive: ImageVector,
) {
    object Home : MenuBottomNav(
        ConstHelper.RouteNames.Home.path,
        "Home",
        Icons.Outlined.Home,
        Icons.Filled.Home
    )
    object Internships : MenuBottomNav(
        ConstHelper.RouteNames.Internships.path,
        "Lowongan",
        Icons.Outlined.Work,
        Icons.Filled.Work
    )
    object MyApplications : MenuBottomNav(
        ConstHelper.RouteNames.MyApplications.path,
        "Lamaran",
        Icons.Outlined.Inbox,
        Icons.Filled.Inbox
    )
    object Profile : MenuBottomNav(
        ConstHelper.RouteNames.Profile.path,
        "Profile",
        Icons.Outlined.Person,
        Icons.Filled.Person
    )
}

@Composable
fun BottomNavComponent(navController: NavHostController) {
    val items: List<MenuBottomNav> = listOf(
        MenuBottomNav.Home,
        MenuBottomNav.Internships,
        MenuBottomNav.MyApplications,
        MenuBottomNav.Profile,
    )

    val currentRoute = navController.currentDestination?.route

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.height(85.dp),
            tonalElevation = 0.dp
        ) {
            items.forEachIndexed { _, screen ->
                val selected = currentRoute?.contains(screen.route) == true
                val animatedHeight by animateDpAsState(
                    targetValue = if (selected) 46.dp else 40.dp,
                    label = "navigationHeight"
                )

                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        RouteHelper.to(navController, screen.route, true)
                    },
                    icon = {
                        NavigationIcon(
                            selected = selected,
                            screen = screen,
                            hasNotification = false,
                            animatedHeight = animatedHeight
                        )
                    },
                    alwaysShowLabel = false,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationIcon(
    selected: Boolean,
    screen: MenuBottomNav,
    hasNotification: Boolean = false,
    animatedHeight: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .size(width = 64.dp, height = animatedHeight),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp)
                    )
            )
        }

        BadgedBox(
            badge = {
                if (hasNotification && !selected) {
                    Badge(
                        modifier = Modifier
                            .size(8.dp)
                            .offset(x = 6.dp, y = (-6).dp),
                        containerColor = MaterialTheme.colorScheme.error
                    )
                }
            }
        ) {
            Icon(
                imageVector = if (selected) screen.iconActive else screen.icon,
                contentDescription = screen.title,
                modifier = Modifier.size(28.dp),
                tint = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, name = "Default")
@Composable
fun PreviewBottomNavComponent() {
    DelcomTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                BottomNavComponent(
                    navController = NavHostController(LocalContext.current)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewBottomNavComponentDark() {
    DelcomTheme(darkTheme = true) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                BottomNavComponent(
                    navController = NavHostController(LocalContext.current)
                )
            }
        }
    }
}