package com.it10x.foodappgstav7_31.ui.menu.restaurant

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.it10x.foodappgstav7_31.SidebarSectionHeader
import com.it10x.foodappgstav7_31.auth.PosSessionManager
import com.it10x.foodappgstav7_31.ui.preview.ReceiptPreviewActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun RestaurantMainMenu(
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
        label = { Text("POS") },
        selected = false,
        onClick = {
            scope.launch { drawerState.close() }
            navController.navigate("pos") {
                popUpTo("pos") { inclusive = true }
            }
        }
    )

    Divider(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 4.dp),
        thickness = 0.5.dp
    )

//    NavigationDrawerItem(
//        label = { Text("🧾 Receipt Preview") },
//        selected = false,
//        onClick = {
//
//            val file = File(
//                context.cacheDir,
//                "receipt_preview.png"
//            )
//
//            Log.d("IMAGE_TEST", "Preview path = ${file.absolutePath}")
//            Log.d("IMAGE_TEST", "Preview exists = ${file.exists()}")
//            Log.d("IMAGE_TEST", "Preview size = ${file.length()}")
//
//            if (!file.exists()) {
//
//                Toast.makeText(
//                    context,
//                    "No preview found. Print a bill first.",
//                    Toast.LENGTH_SHORT
//                ).show()
//
//                return@NavigationDrawerItem
//            }
//
//            val intent = Intent(
//                context,
//                ReceiptPreviewActivity::class.java
//            ).apply {
//                putExtra("image", file.absolutePath)
//            }
//
//            context.startActivity(intent)
//
//            scope.launch { drawerState.close() }
//        }
//    )



    NavigationDrawerItem(
        label = { Text("Tables") },
        selected = false,
        onClick = {
            scope.launch { drawerState.close() }
            navController.navigate("Tables") {
                popUpTo("Tables") { inclusive = true }
            }
        }
    )

    Divider(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 4.dp),
        thickness = 0.5.dp
    )



    NavigationDrawerItem(
        label = { Text("Table Shift") },
        selected = false,
        onClick = {
            scope.launch { drawerState.close() }

            navController.navigate("table_change") {
                launchSingleTop = true
            }
        }
    )

    Divider(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 4.dp),
        thickness = 0.5.dp
    )

    NavigationDrawerItem(
        label = { Text("Waiter Tables View") },
        selected = false,
        onClick = {
            scope.launch { drawerState.close() }
            navController.navigate("waiter_tables_view") {
                launchSingleTop = true
            }
        }
    )

    Divider(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 4.dp),
        thickness = 0.5.dp
    )

    NavigationDrawerItem(
        label = { Text("Online Orders") },
        selected = false,
        onClick = {
            scope.launch { drawerState.close() }
            navController.navigate("orders")
        }
    )

    Divider(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 4.dp),
        thickness = 0.5.dp
    )

    NavigationDrawerItem(
        label = { Text("Local Orders") },
        selected = false,
        onClick = {
            scope.launch { drawerState.close() }
            navController.navigate("local_orders")
        }
    )

    Divider(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 4.dp),
        thickness = 0.5.dp
    )
    NavigationDrawerItem(
        icon = {
            Icon(
                imageVector = Icons.Default.DirectionsRun,
                contentDescription = "Running Orders"
            )
        },
        label = { Text("TW/DL") },
        selected = false,
        onClick = {
            scope.launch {
                drawerState.close()
            }

            navController.navigate("running_orders") {
                launchSingleTop = true
            }
        }
    )

    Divider(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 4.dp),
        thickness = 0.5.dp
    )

    NavigationDrawerItem(
        label = { Text("KOT History") },
        selected = false,
        onClick = {
            scope.launch { drawerState.close() }
            navController.navigate("kot_history")
        }
    )



    // ===============================
    // REPORTS
    // ===============================

    SidebarSectionHeader("REPORTS")

    NavigationDrawerItem(
        label = { Text("Business Day Closing") },
        selected = false,
        onClick = {
            scope.launch { drawerState.close() }
            navController.navigate("day_closing")
        }
    )

    Divider(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 4.dp),
        thickness = 0.5.dp
    )

    NavigationDrawerItem(
        label = { Text("Sales / Z-Report") },
        selected = false,
        onClick = {
            scope.launch { drawerState.close() }
            navController.navigate("sales")
        }
    )

    Divider(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 4.dp),
        thickness = 0.5.dp
    )

    // ===============================
    // CUSTOMERS
    // ===============================

    SidebarSectionHeader("CUSTOMERS")

    NavigationDrawerItem(
        label = { Text("Customer List") },
        selected = false,
        onClick = {
            scope.launch { drawerState.close() }
            navController.navigate("customers")
        }
    )


//    NavigationDrawerItem(
//        label = { Text("Loyalty Program") },
//        selected = false,
//        onClick = {
//            scope.launch {
//                drawerState.close()
//            }
//
//            navController.navigate("loyalty") {
//                launchSingleTop = true
//            }
//        }
//    )

    Divider(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 4.dp),
        thickness = 0.5.dp
    )

    Divider(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 4.dp),
        thickness = 0.5.dp
    )

    NavigationDrawerItem(
        label = { Text("Delivery Settlement") },
        selected = false,
        onClick = {
            scope.launch { drawerState.close() }
            navController.navigate("delivery_settlement")
        }
    )

    Divider(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 4.dp),
        thickness = 0.5.dp
    )

    NavigationDrawerItem(
        label = { Text("Address") },
        selected = false,
        onClick = {
            scope.launch { drawerState.close() }
            navController.navigate("Address")
        }
    )

    // ===============================
    // SYSTEM
    // ===============================

    SidebarSectionHeader("SYSTEM")

    NavigationDrawerItem(
        label = { Text("Sync") },
        selected = false,
        onClick = {
            scope.launch { drawerState.close() }
            navController.navigate("sync_data")
        }
    )

    Divider(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 4.dp),
        thickness = 0.5.dp
    )

    NavigationDrawerItem(
        label = { Text("Settings") },
        selected = false,
        onClick = {
            scope.launch { drawerState.close() }

            navController.navigate("settings") {
                launchSingleTop = true
            }
        }
    )

    NavigationDrawerItem(
        label = { Text("Theme Settings") },
        selected = false,
        onClick = {
            scope.launch { drawerState.close() }

            navController.navigate("theme_settings") {
                launchSingleTop = true
            }
        }
    )

    Divider(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 4.dp),
        thickness = 0.5.dp
    )

    NavigationDrawerItem(
        label = { Text("Printer Settings") },
        selected = false,
        onClick = {
            scope.launch { drawerState.close() }
            navController.navigate("printer_role_selection")
        }
    )

    // ===============================
    // SETUP
    // ===============================

    SidebarSectionHeader("SETUP")

    NavigationDrawerItem(
        label = { Text("DEVICE") },
        selected = false,
        onClick = {
            scope.launch { drawerState.close() }
            navController.navigate("device_role_selection")
        }
    )

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