package com.it10x.foodappgstav7_31.ui.tables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_31.data.online.repository.WaiterTableRepository
import com.it10x.foodappgstav7_31.data.online.sync.TableKotSyncService
import com.it10x.foodappgstav7_31.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_31.viewmodel.PosTableViewModel
import com.it10x.foodappgstav7_31.ui.pos.StatusBadge
import com.it10x.foodappgstav7_31.ui.cart.CartViewModel
import com.it10x.foodappgstav7_31.ui.pos.PosSessionViewModel
import kotlinx.coroutines.launch

@Composable
fun WaiterTableViewScreen(
    navController: NavController,
    cartViewModel: CartViewModel,
    posTableViewModel: PosTableViewModel,
    posSessionViewModel: PosSessionViewModel
) {

    val context = LocalContext.current
    val db = AppDatabaseProvider.get(context)

    val syncService = remember {
        TableKotSyncService(
            firestore = FirebaseFirestore.getInstance(),
            kotItemDao = db.kotItemDao()
        )
    }

    val localTables by posTableViewModel.tables.collectAsState()
    val selectedTableId by posSessionViewModel.tableId.collectAsState()

    var firestoreTables by remember {
        mutableStateOf<Map<String, PosTableViewModel.TableUiState>>(emptyMap())
    }

    val repository = remember {
        WaiterTableRepository(FirebaseFirestore.getInstance())
    }

    // 🔥 Load local
    LaunchedEffect(Unit) {
        posTableViewModel.loadTables()
    }

    // 🔥 Firestore listener
    LaunchedEffect(Unit) {
        repository.startListening { list ->
            firestoreTables = list.associateBy { it.table.id }
        }
    }

    DisposableEffect(Unit) {
        onDispose { repository.stopListening() }
    }

    // ✅ MERGE LOGIC
    val finalTables = remember(localTables, firestoreTables) {

        localTables.map { local ->

            val fs = firestoreTables[local.table.id]

            if (fs != null) {
                // 🔥 Merge Firestore into local
                local.copy(
                    table = local.table.copy(
                        // 👉 you can choose what to override
                        cartCount = fs.table.cartCount // 🔹 from waiter
                        // later: kitchen, bill, etc.
                    )
                )
            } else {
                local
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        WaiterTableViewGrid(
            tables = finalTables,
            selectedTable = selectedTableId,

            // 🔹 existing click
            onTableClick = { tableId ->
                val table = finalTables.first { it.table.id == tableId }.table

                posSessionViewModel.setTable(
                    tableId = table.id,
                    tableName = table.tableName
                )

                cartViewModel.initSession("DINE_IN", table.id, table.tableName)

                navController.navigate("pos") {
                    launchSingleTop = true
                }
            },

            // 🔥 NEW: Sync button click
            onSyncClick = { tableId ->
                // 🔥 Call suspend function
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    syncService.syncTableSnapshot(
                        tableId = tableId,
                        source = "POS"
                    )
                }
            }
        )
    }
}