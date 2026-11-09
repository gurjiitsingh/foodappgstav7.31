package com.it10x.foodappgstav7_31.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.it10x.foodappgstav7_31.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_31.data.pos.entities.config.OutletEntity
import com.it10x.foodappgstav7_31.ui.settings.components.SettingsSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutletInformationScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val db = AppDatabaseProvider.get(context)

    var outlet by remember {
        mutableStateOf<OutletEntity?>(null)
    }

    LaunchedEffect(Unit) {
        outlet = db.outletDao().getOutlet()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Outlet Information")
                },
                navigationIcon = {
                    TextButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Text("Back")
                    }
                }
            )
        }
    ) { paddingValues ->

        if (outlet == null) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }

        } else {

            val data = outlet!!

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ------------------------------------------------
                // BASIC INFORMATION
                // ------------------------------------------------

                item {
                    SettingsSection(
                        title = "BASIC INFORMATION"
                    ) {

                        OutletInfoItem(
                            title = "Outlet Name",
                            value = data.outletName
                        )

                        OutletInfoItem(
                            title = "Outlet ID",
                            value = data.outletId
                        )

                        OutletInfoItem(
                            title = "Owner ID",
                            value = data.ownerId
                        )

                        OutletInfoItem(
                            title = "POS Type",
                            value = data.posType
                        )
                    }
                }

                // ------------------------------------------------
                // ADDRESS
                // ------------------------------------------------

                item {
                    SettingsSection(
                        title = "ADDRESS"
                    ) {

                        OutletInfoItem(
                            title = "Address Line 1",
                            value = data.addressLine1
                        )

                        OutletInfoItem(
                            title = "Address Line 2",
                            value = data.addressLine2
                        )

                        OutletInfoItem(
                            title = "Address Line 3",
                            value = data.addressLine3
                        )

                        OutletInfoItem(
                            title = "City",
                            value = data.city
                        )

                        OutletInfoItem(
                            title = "State",
                            value = data.state
                        )

                        OutletInfoItem(
                            title = "Zipcode",
                            value = data.zipcode
                        )

                        OutletInfoItem(
                            title = "Country",
                            value = data.countryName
                        )
                    }
                }

                // ------------------------------------------------
                // CONTACT
                // ------------------------------------------------

                item {
                    SettingsSection(
                        title = "CONTACT"
                    ) {

                        OutletInfoItem(
                            title = "Phone",
                            value = data.phone
                        )

                        OutletInfoItem(
                            title = "Phone 2",
                            value = data.phone2
                        )

                        OutletInfoItem(
                            title = "Email",
                            value = data.email
                        )

                        OutletInfoItem(
                            title = "Website",
                            value = data.web
                        )
                    }
                }

                // ------------------------------------------------
                // TAX
                // ------------------------------------------------

                item {
                    SettingsSection(
                        title = "TAX INFORMATION"
                    ) {

                        OutletInfoItem(
                            title = "Tax Type",
                            value = data.taxType
                        )

                        OutletInfoItem(
                            title = "Tax Mode",
                            value = data.taxMode
                        )

                        OutletInfoItem(
                            title = "GST / VAT Number",
                            value = data.gstVatNumber
                        )

                        OutletInfoItem(
                            title = "FSSAI Number",
                            value = data.fssaiNumber
                        )
                    }
                }

                // ------------------------------------------------
                // COUNTRY & CURRENCY
                // ------------------------------------------------

                item {
                    SettingsSection(
                        title = "COUNTRY & CURRENCY"
                    ) {

                        OutletInfoItem(
                            title = "Country Code",
                            value = data.countryCode
                        )

                        OutletInfoItem(
                            title = "Currency",
                            value = data.currencyCode
                        )

                        OutletInfoItem(
                            title = "Locale",
                            value = data.localeTag
                        )
                    }
                }

                // ------------------------------------------------
                // PRINTER
                // ------------------------------------------------

                item {
                    SettingsSection(
                        title = "PRINTER"
                    ) {

                        OutletInfoItem(
                            title = "Printer Width",
                            value = "${data.printerWidth} mm"
                        )

                        OutletInfoItem(
                            title = "Printer Name",
                            value = data.printerName
                        )

                        OutletInfoItem(
                            title = "Kitchen Printer IP",
                            value = data.printerIPKitchen
                        )

                        OutletInfoItem(
                            title = "Bill Printer IP",
                            value = data.printerIPBill
                        )

                        OutletInfoItem(
                            title = "Footer Note",
                            value = data.footerNote
                        )
                    }
                }

                // ------------------------------------------------
                // QR
                // ------------------------------------------------

                item {
                    SettingsSection(
                        title = "QR"
                    ) {

                        OutletInfoItem(
                            title = "QR Enabled",
                            value = if (data.qrEnabled) "Yes" else "No"
                        )

                        OutletInfoItem(
                            title = "QR Title",
                            value = data.qrTitle
                        )

                        OutletInfoItem(
                            title = "QR Text",
                            value = data.qrText
                        )
                    }
                }

                // ------------------------------------------------
                // UPI
                // ------------------------------------------------

                item {
                    SettingsSection(
                        title = "UPI"
                    ) {

                        OutletInfoItem(
                            title = "UPI ID",
                            value = data.upiId
                        )

                        OutletInfoItem(
                            title = "UPI Name",
                            value = data.upiName
                        )

                        OutletInfoItem(
                            title = "UPI Title",
                            value = data.upiTitle
                        )
                    }
                }

                // ------------------------------------------------
                // POS SETTINGS
                // ------------------------------------------------

                item {
                    SettingsSection(
                        title = "POS SETTINGS"
                    ) {

                        OutletInfoItem(
                            title = "Category Sidebar",
                            value = if (data.showCategorySidebar) {
                                "Shown"
                            } else {
                                "Hidden"
                            }
                        )

                        OutletInfoItem(
                            title = "Startup Screen",
                            value = when (data.startupScreen) {
                                "tables" -> "Table Screen"
                                "pos" -> "POS Screen"
                                else -> data.startupScreen
                            }
                        )
                    }
                }

                // ------------------------------------------------
                // STATUS
                // ------------------------------------------------

                item {
                    SettingsSection(
                        title = "STATUS"
                    ) {

                        OutletInfoItem(
                            title = "Status",
                            value = if (data.isActive) {
                                "Active"
                            } else {
                                "Inactive"
                            }
                        )

                        OutletInfoItem(
                            title = "Created At",
                            value = data.createdAt?.toString()
                        )

                        OutletInfoItem(
                            title = "Updated At",
                            value = data.updatedAt?.toString()
                        )
                    }
                }

                item {
                    Spacer(
                        modifier = Modifier.height(32.dp)
                    )
                }
            }
        }
    }

}

@Composable
private fun OutletInfoItem(
    title: String,
    value: String?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {

        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(2.dp)
        )

        Text(
            text = value?.takeIf { it.isNotBlank() } ?: "—",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}