package com.it10x.foodappgstav7_31.com.it10x.foodappgstav7_31.ui.tables


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_31.viewmodel.PosTableViewModel
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.it10x.foodappgstav7_31.ui.pos.StatusBadge
import com.it10x.foodappgstav7_31.ui.theme.PosTheme

//*****************   used in topbar table **********************//
@Composable
fun TableGridContent(
    tables: List<PosTableViewModel.TableUiState>,
    selectedTable: String?,
    navController: NavController,
    onTableSelected: (String) -> Unit,
    onTransferClick: (String) -> Unit
) {

    val groupedByArea = tables
        .groupBy { it.table.area ?: "General" }
        .toSortedMap()

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {

        groupedByArea.forEach { (areaName, areaTables) ->

            // 🔹 AREA HEADER (Full Width)
            item(
                span = { GridItemSpan(maxLineSpan) }
            ) {
                Text(
                    text = areaName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // 🔹 TABLES
            items(
                items = areaTables.sortedBy { it.table.sortOrder ?: Int.MAX_VALUE },
                key = { it.table.id }
            ) { ui ->

                val table = ui.table
                val isSelected = selectedTable == table.id

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    tonalElevation = 2.dp,
                    border = if (isSelected)
                        BorderStroke(2.dp, Color(0xFFFF9800))
                    else null,
                    modifier = Modifier
                        .aspectRatio(.60f)
                        .clickable {
                            onTableSelected(table.id)
                        }
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    ) {

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {

                            // OPEN POS BUTTON
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PosTheme.accent.cartAddBg,   // ✅ use existing color
                                shadowElevation = 3.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onTableSelected(table.id)
                                        navController.navigate("pos") {
                                            launchSingleTop = true
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Icon(
                                        imageVector = Icons.Default.PointOfSale,
                                        contentDescription = null,
                                        tint = PosTheme.accent.cartAddText, // ✅ text color
                                        modifier = Modifier.size(18.dp)
                                    )

                                    Spacer(Modifier.width(6.dp))

                                    Text(
                                        text = table.tableName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PosTheme.accent.cartAddText // ✅ text color
                                    )
                                }
                            }

                            // TRANSFER BUTTON
//                            Spacer(Modifier.width(8.dp))
//                            Surface(
//                                shape = RoundedCornerShape(6.dp),
//                                border = BorderStroke(
//                                    1.dp,
//                                    MaterialTheme.colorScheme.primary
//                                ),
//                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .clickable {
//                                        onTableSelected(table.id)
//                                        onTransferClick(table.id)
//                                    }
//                            )
//                            {
//                                Row(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .padding(horizontal = 8.dp, vertical = 16.dp),
//                                    verticalAlignment = Alignment.CenterVertically
//                                ) {
//
//                                    Icon(
//                                        imageVector = Icons.Default.SwapHoriz,
//                                        contentDescription = "Transfer Table",
//                                        tint = MaterialTheme.colorScheme.primary,
//                                        modifier = Modifier.size(16.dp)
//                                    )
//
//                                    Spacer(Modifier.width(6.dp))
//
//                                    Text(
//                                        text = table.tableName,
//                                        style = MaterialTheme.typography.bodyMedium,
//                                        fontWeight = FontWeight.Bold,
//                                        color = MaterialTheme.colorScheme.primary
//                                    )
//                                }
//                            }

                            // CART BADGE SLOT (FULL WIDTH)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(30.dp)
                                    .padding(horizontal = 2.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (ui.cartCount > 0) {
                                    StatusBadge(
                                        icon = "🛒",
                                        text = ui.cartCount.toString(),
                                        bgColor = Color(0xFF1976D2).copy(alpha = 0.6f),
                                        modifier = Modifier.fillMaxWidth() // <- full width
                                    )
                                }
                            }

// BILL BADGE SLOT (FULL WIDTH)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(28.dp)
                                    .padding(horizontal = 2.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (ui.billDoneCount > 0) {
                                    StatusBadge(
                                        icon = "🧾",
                                        text = ui.billDoneCount.toString(),
                                        bgColor = Color(0xFF2E7D32).copy(alpha = 0.6f),
                                        modifier = Modifier.fillMaxWidth() // <- full width
                                    )
                                }
                            }


                        }
                    }
                }
            }
        }
    }

}




