package com.it10x.foodappgstav7_31.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.it10x.foodappgstav7_31.ui.settings.components.SettingsItem
import com.it10x.foodappgstav7_31.ui.settings.components.SettingsSection
import com.it10x.foodappgstav7_31.ui.settings.components.SettingsToggle
import androidx.compose.ui.platform.LocalContext
import com.it10x.foodappgstav7_31.data.pos.AppDatabaseProvider
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    navController: NavController
) {

    val context = LocalContext.current
    val db = AppDatabaseProvider.get(context)

    var showCategorySidebar by remember {
        mutableStateOf(true)
    }

    var startupScreen by remember {
        mutableStateOf("tables")
    }

    val scope = rememberCoroutineScope()


    LaunchedEffect(Unit) {
        val outlet = db.outletDao().getOutlet()

        showCategorySidebar =
            outlet?.showCategorySidebar ?: true

        startupScreen =
            outlet?.startupScreen ?: "tables"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {

            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {

            SettingsSection(
                title = "POS SETTINGS"
            ) {

                SettingsToggle(
                    title = "Show Category Sidebar",
                    subtitle = "Display categories on left side",
                    checked = showCategorySidebar,
                    onCheckedChange = { enabled ->

                        showCategorySidebar = enabled

                        scope.launch {

                            val outlet = db.outletDao().getOutlet()

                            if (outlet != null) {

                                val updatedOutlet = outlet.copy(
                                    showCategorySidebar = enabled
                                )

                                // ✅ SAVE ROOM
                                db.outletDao().saveOutlet(updatedOutlet)

                                // ✅ SAVE FIRESTORE
                                com.google.firebase.firestore.FirebaseFirestore
                                    .getInstance()
                                    .collection("outlets")
                                    .document(outlet.outletId)
                                    .update(
                                        mapOf(
                                            "showCategorySidebar" to enabled,
                                            "startupScreen" to outlet.startupScreen
                                        )
                                    )
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Startup Screen",
                    style = MaterialTheme.typography.titleMedium
                )



                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ){

                    FilterChip(
                        selected = startupScreen == "tables",
                        onClick = {

                            startupScreen = "tables"

                            scope.launch {

                                val outlet = db.outletDao().getOutlet()

                                if (outlet != null) {

                                    val updatedOutlet = outlet.copy(
                                        startupScreen = "tables"
                                    )

                                    db.outletDao().saveOutlet(updatedOutlet)

                                    com.google.firebase.firestore.FirebaseFirestore
                                        .getInstance()
                                        .collection("outlets")
                                        .document(outlet.outletId)
                                        .update(
                                            mapOf(
                                                "showCategorySidebar" to outlet.showCategorySidebar,
                                                "startupScreen" to "tables"
                                            )
                                        )
                                }
                            }
                        },
                        label = {
                            Text("Table Screen")
                        }
                    )

                    FilterChip(
                        selected = startupScreen == "pos",
                        onClick = {

                            startupScreen = "pos"

                            scope.launch {

                                val outlet = db.outletDao().getOutlet()

                                if (outlet != null) {

                                    val updatedOutlet = outlet.copy(
                                        startupScreen = "pos"
                                    )

                                    db.outletDao().saveOutlet(updatedOutlet)

                                    com.google.firebase.firestore.FirebaseFirestore
                                        .getInstance()
                                        .collection("outlets")
                                        .document(outlet.outletId)
                                        .update(
                                            mapOf(
                                                "showCategorySidebar" to outlet.showCategorySidebar,
                                                "startupScreen" to "pos"
                                            )
                                        )
                                }
                            }
                        },
                        label = {
                            Text("POS Screen")
                        }
                    )
                }

                SettingsItem(
                    title = "POS Layout",
                    subtitle = "Grid size and layout options"
                ) {

                }

                SettingsItem(
                    title = "Cart Settings",
                    subtitle = "Cart panel and behavior"
                ) {

                }

                SettingsSection(
                    title = "OUTLET"
                ) {

                    SettingsItem(
                        title = "Outlet",
                        subtitle =  "Outlet information"
                    ) {
                        navController.navigate("outlet_information")
                    }
                }
            }
        }

        item {

            SettingsSection(
                title = "PRINTER"
            ) {

                SettingsItem(
                    title = "Printer Settings",
                    subtitle = "Kitchen and receipt printers"
                ) {
                    navController.navigate("printer_role_selection")
                }
            }
        }

        item {

            SettingsSection(
                title = "APPEARANCE"
            ) {

                SettingsItem(
                    title = "Theme Settings",
                    subtitle = "Dark mode and colors"
                ) {
                    navController.navigate("theme_settings")
                }
            }
        }

        item {

            SettingsSection(
                title = "DEVICE"
            ) {

                SettingsItem(
                    title = "Device Role",
                    subtitle = "Main POS or Waiter mode"
                ) {
                    navController.navigate("device_role_selection")
                }
            }
        }

        item {

            SettingsSection(
                title = "DATA & SYNC"
            ) {

                SettingsItem(
                    title = "Sync Data",
                    subtitle = "Sync products and orders"
                ) {
                    navController.navigate("sync_data")
                }
            }
        }

        item {

            SettingsSection(
                title = "ADVANCED"
            ) {

                SettingsItem(
                    title = "Advanced Settings",
                    subtitle = "Technical and debug settings"
                ) {
                    navController.navigate("advanced_settings")
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))

            SettingsSection(
                title = "DATA & SYNC"
            ) {

                SettingsItem(
                    title = "Sync Data",
                    subtitle = "Sync products and orders"
                ) {
                    navController.navigate("sync_data")
                }

                SettingsItem(
                    title = "Export Data",
                    subtitle = "Export POS orders as JSON"
                ) {
                    navController.navigate("export_data")
                }
            }
        }
    }
}