package com.it10x.foodappgstav7_31.ui.pos

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_31.data.pos.entities.VirtualTableEntity
import com.it10x.foodappgstav7_31.viewmodel.VirtualTableViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunningOrdersScreen(
    viewModel: VirtualTableViewModel,
    onTableSelected: (VirtualTableEntity) -> Unit,
    onClose: () -> Unit
) {

    var selectedType by remember {
        mutableStateOf("TAKEAWAY")
    }

    // Tell existing ViewModel what we want to observe
    LaunchedEffect(selectedType) {
        viewModel.setOrderType(selectedType)
    }

    val tables by viewModel.tables.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.logAllTableStatuses()
    }
    LaunchedEffect(tables, selectedType) {
        tables.forEach {
            Log.d(
                "VIRTUAL_TABLE",
                "type=${it.orderType}, name=${it.tableName}, status=${it.status}"
            )
        }
    }

    // Only show TW / DL
    val runningTables = tables.filter {
        it.status == VirtualTableViewModel.STATUS_RUNNING ||
                it.status == VirtualTableViewModel.STATUS_NEW
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // =====================================================
        // HEADER
        // =====================================================

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "Running Orders",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onClose
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close"
                )
            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        // =====================================================
        // TW / DL SWITCH
        // =====================================================

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            FilterChip(
                selected = selectedType == "TAKEAWAY",
                onClick = {
                    selectedType = "TAKEAWAY"
                },
                label = {
                    Text("TW")
                },
                modifier = Modifier.weight(1f)
            )

            FilterChip(
                selected = selectedType == "DELIVERY",
                onClick = {
                    selectedType = "DELIVERY"
                },
                label = {
                    Text("DL")
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // =====================================================
        // RUNNING TABLES
        // =====================================================

        if (runningTables.isEmpty()) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (selectedType == "TAKEAWAY")
                        "No running takeaway orders"
                    else
                        "No running delivery orders"
                )
            }

        } else {

            LazyVerticalGrid(
                columns = GridCells.Adaptive(
                    minSize = 110.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {

                items(
                    items = runningTables,
                    key = { it.id }
                ) { table ->

                    VirtualTableCard(
                        table = table,
                        onClick = {
                            onTableSelected(table)
                        }
                    )
                }
            }
        }
    }


}

@Composable
fun VirtualTableCard(
    table: VirtualTableEntity,
    onClick: () -> Unit
) {

    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable() {
                onClick()
            },
        shape = RoundedCornerShape(12.dp),
        color = if (table.orderType == "TAKEAWAY") {
            Color(0xFF81C784)
        } else {
            Color(0xFFFFD54F)
        },
        tonalElevation = 2.dp
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = table.tableName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {

                if (table.cartCount > 0) {
                    StatusBadge(
                        icon = "🛒",
                        text = table.cartCount.toString(),
                        bgColor = Color(0xFF1976D2).copy(alpha = 0.55f),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (table.billCount > 0) {
                    StatusBadge(
                        icon = "🧾",
                        text = table.billCount.toString(),
                        bgColor = Color(0xFF2E7D32).copy(alpha = 0.55f),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}