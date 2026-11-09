package com.it10x.foodappgstav7_31.ui.settings

import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_31.data.PrinterPreferences
import com.it10x.foodappgstav7_31.data.online.sync.CustomerSyncViewModelFactory
import com.it10x.foodappgstav7_31.data.online.sync.PrinterSyncRepository
import com.it10x.foodappgstav7_31.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_31.data.pos.repository.PrinterRepository
import com.it10x.foodappgstav7_31.data.printer.PrinterRestoreManager
import com.it10x.foodappgstav7_31.viewmodel.CustomerSyncViewModel
import com.it10x.foodappgstav7_31.viewmodel.InventorySyncViewModel
import com.it10x.foodappgstav7_31.viewmodel.OrderSyncViewModel
import com.it10x.foodappgstav7_31.viewmodel.OrderSyncViewModelFactory
import com.it10x.foodappgstav7_31.viewmodel.OutletSyncViewModel
import com.it10x.foodappgstav7_31.viewmodel.ProductSyncViewModel
import com.it10x.foodappgstav7_31.viewmodel.TableSyncViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SyncScreen(
    navController: NavController,
    onBack: () -> Unit = {}
) {
    val productVm: ProductSyncViewModel = viewModel()
    val outletVm: OutletSyncViewModel = viewModel()
    val tableVm: TableSyncViewModel = viewModel()
    val inventorySyncVm: InventorySyncViewModel = viewModel()

    val productSyncing by productVm.syncing.collectAsState()
    val productStatus by productVm.status.collectAsState()

    val outletSyncing by outletVm.syncing.collectAsState()
    val outletStatus by outletVm.status.collectAsState()

    val tableSyncing by tableVm.syncing.collectAsState()
    val tableStatus by tableVm.status.collectAsState()

    val context = LocalContext.current
    val application = context.applicationContext as Application

    val orderSyncVm: OrderSyncViewModel = viewModel(
        factory = OrderSyncViewModelFactory(application)
    )

    val customerSyncVm: CustomerSyncViewModel = viewModel(
        factory = CustomerSyncViewModelFactory(application)
    )

    val inventorySyncing by inventorySyncVm.isSyncing.collectAsState()
    val inventoryStatus by inventorySyncVm.status.collectAsState()

    val customerSyncing by customerSyncVm.syncing.collectAsState()
    val customerStatus by customerSyncVm.status.collectAsState()

    val orderSyncing by orderSyncVm.syncing.collectAsState()
    val orderSyncStatus by orderSyncVm.status.collectAsState()

    val db = remember { AppDatabaseProvider.get(context) }

    val printerRepository = remember {
        PrinterRepository(db.printerDao())
    }

    val printerPreferences = remember {
        PrinterPreferences(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Data Sync",
            style = MaterialTheme.typography.titleLarge
        )

        // =====================================================
        // DOWNLOAD FROM FIRESTORE
        // =====================================================

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Download",
                    style = MaterialTheme.typography.titleMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SmallSyncButton(
                        enabled = !outletSyncing && !productSyncing,
                        text = if (outletSyncing)
                            "Syncing Outlet…"
                        else
                            "Sync Outlet Config",
                        success = isSyncSuccessful(outletStatus),
                        onClick = { outletVm.syncOutlet() }
                    )

                    SmallSyncButton(
                        enabled = !productSyncing && !outletSyncing,
                        text = if (productSyncing)
                            "Syncing Menu…"
                        else
                            "Sync Menu Data",
                        success = isSyncSuccessful(productStatus),
                        onClick = { productVm.syncAll() }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SmallSyncButton(
                        enabled = !productSyncing &&
                                !outletSyncing &&
                                !tableSyncing,
                        text = if (tableSyncing)
                            "Syncing Tables…"
                        else
                            "Sync Tables",
                        success = isSyncSuccessful(tableStatus),
                        onClick = { tableVm.syncTables() }
                    )

                    SmallSyncButton(
                        enabled = !customerSyncing &&
                                !orderSyncing &&
                                !productSyncing &&
                                !outletSyncing &&
                                !tableSyncing,
                        text = if (customerSyncing)
                            "Syncing Customers…"
                        else
                            "Sync Customers",
                        success = isSyncSuccessful(customerStatus),
                        onClick = { customerSyncVm.syncCustomers() }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SmallSyncButton(
                        enabled = !productSyncing &&
                                !outletSyncing &&
                                !tableSyncing,
                        text = "Sync Printers",
                        onClick = {
                            Log.d("PRINTER_SYNC", "Button clicked")

                            if (FirebaseApp.getApps(context).isEmpty()) {
                                Log.e(
                                    "PRINTER_SYNC",
                                    "Firebase NOT initialized"
                                )
                                return@SmallSyncButton
                            }

                            Log.d(
                                "PRINTER_SYNC",
                                "Firebase initialized"
                            )

                            val printerSyncRepository =
                                PrinterSyncRepository(
                                    FirebaseFirestore.getInstance(),
                                    printerRepository
                                )

                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    Log.d(
                                        "PRINTER_SYNC",
                                        "Starting download"
                                    )

                                    printerSyncRepository.downloadPrinters()

                                    Log.d(
                                        "PRINTER_SYNC",
                                        "Download finished"
                                    )

                                    val printers =
                                        printerRepository.getAll()

                                    Log.d(
                                        "PRINTER_SYNC",
                                        "Printers in DB: ${printers.size}"
                                    )

                                    PrinterRestoreManager.restoreToPreferences(
                                        printers,
                                        printerPreferences
                                    )

                                    Log.d(
                                        "PRINTER_SYNC",
                                        "Preferences restored"
                                    )
                                } catch (e: Exception) {
                                    Log.e(
                                        "PRINTER_SYNC",
                                        "ERROR: ${e.message}",
                                        e
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }

        // =====================================================
        // BACKUP / UPLOAD TO FIRESTORE
        // =====================================================

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Backup",
                    style = MaterialTheme.typography.titleMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SmallSyncButton(
                        enabled = !orderSyncing &&
                                !productSyncing &&
                                !outletSyncing &&
                                !tableSyncing,
                        text = if (orderSyncing)
                            "Syncing Orders…"
                        else
                            "Sync POS Orders",
                        success = isSyncSuccessful(orderSyncStatus),
                        onClick = {
                            orderSyncVm.syncOrders()
                            customerSyncVm.syncCustomers()
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SmallSyncButton(
                        enabled = !inventorySyncing &&
                                !productSyncing &&
                                !outletSyncing &&
                                !tableSyncing,
                        text = if (inventorySyncing)
                            "Syncing Inventory…"
                        else
                            "Sync Inventory",
                        success = isSyncSuccessful(inventoryStatus),
                        onClick = {
                            inventorySyncVm.syncInventoryNow()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}

@Composable
fun SmallSyncButton(
    enabled: Boolean,
    text: String,
    success: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .width(200.dp)
            .height(42.dp),
        contentPadding = PaddingValues(
            horizontal = 12.dp,
            vertical = 4.dp
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Reserved space prevents the button content shifting.
            Box(
                modifier = Modifier.width(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (success) {
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

private fun isSyncSuccessful(status: String): Boolean {
    val message = status.trim().lowercase()

    if (message.isEmpty()) return false

    // Never show a checkmark for a failed sync.
    if (
        message.contains("failed") ||
        message.contains("error")
    ) {
        return false
    }

    return message.contains("complete") ||
            message.contains("successfully") ||
            message.contains("completed")
}