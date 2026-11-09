package com.it10x.foodappgstav7_31.ui.menu.restaurant

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.it10x.foodappgstav7_31.SidebarSectionHeader
import com.it10x.foodappgstav7_31.auth.PosSessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun WaiterMenu(
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope,
    onLogout: () -> Unit
) {

    val context = LocalContext.current
    var showLogoutDialog by remember {
        mutableStateOf(false)
    }
    // ===============================
    // OPERATIONS
    // ===============================
    SidebarSectionHeader("OPERATIONS")



    NavigationDrawerItem(
        icon = {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = null
            )
        },
        label = {
            Text("Logout")
        },
        selected = false,
        onClick = {
            showLogoutDialog = true
        }
    )

    NavigationDrawerItem(
        label = { Text("Waiter") },
        selected = false,
        onClick = {
            scope.launch { drawerState.close() }
            navController.navigate("posWaiter") {
                popUpTo("posWaiter") { inclusive = true }
            }
        }
    )


//    NavigationDrawerItem(
//        label = { Text("Local Orders") },
//        selected = false,
//        onClick = {
//            scope.launch { drawerState.close() }
//            navController.navigate("local_orders")
//        }
//    )


    // ===============================
    // SYNC & DATA
    // ===============================
    SidebarSectionHeader("SYNC & DATA")

    NavigationDrawerItem(
        label = { Text("Sync") },
        selected = false,
        onClick = {
            scope.launch { drawerState.close() }
            navController.navigate("sync_data")
        }
    )





    // ===============================
    // SETTINGS
    // ===============================
    SidebarSectionHeader("SETTINGS")


    Divider(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 4.dp),
        thickness = 0.5.dp
    )
    NavigationDrawerItem(
        label = { Text("Advanced Settings") },
        selected = false,
        onClick = {
            scope.launch { drawerState.close() }
            navController.navigate("advanced_settings")
        }
    )

    Divider(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 4.dp),
        thickness = 0.5.dp
    )

    NavigationDrawerItem(
        label = { Text("Theme Settings") },
        selected = false,
        onClick = {
            scope.launch { drawerState.close() }
            navController.navigate("theme_settings")
        }
    )

 //   SidebarSectionHeader("SETUP")

//    NavigationDrawerItem(
//        label = { Text("DEVICE") },
//        selected = false,
//        onClick = {
//            scope.launch { drawerState.close() }
//            navController.navigate("device_role_selection")
//        }
//    )

    Divider(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 4.dp),
        thickness = 0.5.dp
    )

    if (showLogoutDialog) {

        AlertDialog(
            onDismissRequest = {
                showLogoutDialog = false
            },
            title = {
                Text("Logout")
            },
            text = {
                Text("Are you sure you want to logout?")
            },
            confirmButton = {
                TextButton(
                    onClick = {

                        showLogoutDialog = false

                        PosSessionManager.logout(context)

                        onLogout()

                        scope.launch {
                            drawerState.close()
                        }
                    }
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

}

