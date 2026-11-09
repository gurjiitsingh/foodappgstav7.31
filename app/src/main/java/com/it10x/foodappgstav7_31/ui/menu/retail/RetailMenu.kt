package com.it10x.foodappgstav7_31.ui.menu.retail

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.it10x.foodappgstav7_31.SidebarSectionHeader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun RetailMenu(
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope
)  {
    // ===============================
    // OPERATIONS
    // ===============================
    SidebarSectionHeader("OPERATIONS")

    NavigationDrawerItem(
        label = { Text("RETAIL") },
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




    // ===============================
    // SALES / Z-REPORT
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
            navController.navigate("sales") // opens SalesScreen
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
            navController.navigate("delivery_settlement") }
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
            navController.navigate("Address") }
    )

    // ===============================
    // SYNC & DATA
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

    // ===============================
    // SETTINGS
    // ===============================
    //  SidebarSectionHeader("SETTINGS")

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
        label = { Text("Printer Settings") },
        selected = false,
        onClick = {
            scope.launch { drawerState.close() }
            navController.navigate("printer_role_selection")
        }
    )

//                                    Divider(
//                                        modifier = Modifier
//                                            .padding(horizontal = 16.dp)
//                                            .padding(bottom = 4.dp),
//                                        thickness = 0.5.dp
//                                    )
//                                    NavigationDrawerItem(
//                                        label = { Text("Advanced Settings") },
//                                        selected = false,
//                                        onClick = {
//                                            scope.launch { drawerState.close() }
//                                            navController.navigate("advanced_settings")
//                                        }
//                                    )

//                                    Divider(
//                                        modifier = Modifier
//                                            .padding(horizontal = 16.dp)
//                                            .padding(bottom = 4.dp),
//                                        thickness = 0.5.dp
//                                    )
//
//                                    NavigationDrawerItem(
//                                        label = { Text("Theme Settings") },
//                                        selected = false,
//                                        onClick = {
//                                            scope.launch { drawerState.close() }
//                                            navController.navigate("theme_settings")
//                                        }
//                                    )

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
}