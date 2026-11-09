package com.it10x.foodappgstav7_31.ui.tables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant

import androidx.compose.material3.*

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.it10x.foodappgstav7_31.viewmodel.PosTableViewModel


@Composable
fun WaiterTableViewGrid(
    tables: List<PosTableViewModel.TableUiState>,
    selectedTable: String?,
    onTableClick: (String) -> Unit,
    onSyncClick: (String) -> Unit
) {

    val groupedByArea = tables
        .groupBy { it.table.area ?: "Waiter" }
        .mapValues { (_, list) ->
            list.sortedBy { it.table.sortOrder ?: Int.MAX_VALUE }
        }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 95.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {

        groupedByArea.forEach { (area, areaTables) ->

            // 🔹 AREA HEADER
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = area,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }

            items(
                items = areaTables,
                key = { it.table.id }
            ) { ui ->

                val table = ui.table
                val isSelected = selectedTable == table.id
                val isMismatch = ui.billDoneCount != ui.table.cartCount
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 2.dp,
                    border = when {
                        isSelected -> BorderStroke(2.dp, Color(0xFFFF9800)) // selected
                        isMismatch -> BorderStroke(2.dp, Color.Red)         // mismatch
                        else -> null
                    },
                    modifier = Modifier
                        .aspectRatio(0.9f)
                        .clickable { onTableClick(table.id) }
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {

                        // 🔹 TABLE NAME (TOP)
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { onSyncClick(table.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {

                                    // 🔹 LEFT → Icon + Table Name
                                    Row(verticalAlignment = Alignment.CenterVertically) {



                                        Text(
                                            text = table.tableName,
                                            style = MaterialTheme.typography.labelMedium,
                                            maxLines = 1
                                        )
                                    }

                                    // 🔹 RIGHT → Sync Icon
                                    Text(
                                        text = "🔄", // simple + fast
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }


                        }



                        // 🔹 BADGES (BOTTOM) - COMPACT
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            // ✅ POS (Local)
                            if (ui.billDoneCount > 0) {
                                StatusBadgeWaiter(
                                    icon = "🧾",
                                    label = "POS",
                                    text = ui.billDoneCount.toString(),
                                    bgColor = Color(0xFF2E7D32).copy(alpha = 0.7f),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // ✅ WAITER (Firestore)
                            if (ui.table.cartCount > 0) {
                                StatusBadgeWaiter(
                                    icon = "👨‍🍳",
                                    label = "Waiter",
                                    text = ui.table.cartCount.toString(),
                                    bgColor = Color(0xFF1976D2).copy(alpha = 0.7f),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun StatusBadgeWaiter(
    icon: String,
    label: String,
    text: String,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(bgColor, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp), // ✅ compact height
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = "$icon $label",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}