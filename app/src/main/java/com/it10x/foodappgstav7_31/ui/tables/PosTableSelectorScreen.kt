package com.it10x.foodappgstav7_31.ui.pos


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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.it10x.foodappgstav7_31.ui.theme.PosTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import android.util.Log
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun TableSelectorGrid(
    tables: List<PosTableViewModel.TableUiState>,
    selectedTable: String?,
    onTableSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // val groupedByArea = tables.groupBy { it.table.area ?: "General" }

    // ✅ Group tables by area and sort by sortOrder
    val groupedByArea = tables
        .groupBy { it.table.area ?: "General" }
        .mapValues { (_, areaTables) ->
            areaTables.sortedBy { it.table.sortOrder ?: Int.MAX_VALUE }
        }
    val configuration = LocalConfiguration.current
    val isPhone = configuration.screenWidthDp < 600

    val tableGreen = Color(0xFF2CAC44)
    val tableBlue = Color(0xFF17A6F2)
    val tableRed = Color(0xFFF35260)
    val tableGray = Color(0xFF585E63)

//    val currencySymbol = java.util.Currency
//        .getInstance(currencyCode)
//        .symbol

// TABLET
    // TABLET

    if (!isPhone) {
        val screenWidth = LocalConfiguration.current.screenWidthDp

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(0.dp),
                color = PosTheme.product.productCardBg,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // HEADER
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Dine in Tables",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = PosTheme.product.productCardText
                            )
                        )

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = PosTheme.product.productCardText
                            )
                        }
                    }

                    // TABLES GROUPED BY AREA
                    groupedByArea.entries.forEach { (areaName, areaTables) ->

                        Text(
                            text = areaName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PosTheme.product.productCardText
                            ),
                            modifier = Modifier
                                .padding(vertical = 5.dp)
                                .fillMaxWidth()
                        )

                        // GRID SIZE
                        val availableWidth = screenWidth - 32
                        val columns = (availableWidth / 148).coerceAtLeast(1)
                        val rows =
                            (areaTables.size + columns - 1) / columns

                        val gridHeight =
                            (rows * 114 + (rows - 1) * 8).dp

                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 140.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(gridHeight)
                                .padding(bottom = 5.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            userScrollEnabled = false
                        ) {
                            items(areaTables) { ui ->

                                val table = ui.table
                                val isSelected = selectedTable == table.id

                                // COLORS — SAME PRIORITY AS TableChangeCard
                                val bgColor = when {
                                    isSelected ->
                                        Color(0xFF16A34A)

                                    ui.billDoneCount > 0 ->
                                        Color(0xFFE57373)

                                    ui.kitchenPendingCount > 0 ->
                                        Color(0xFF81C784)

                                    ui.cartCount > 0 ->
                                        Color(0xFF64B5F6)

                                    else ->
                                        MaterialTheme.colorScheme.surfaceVariant
                                }

                                val contentColor = when {
                                    isSelected ->
                                        MaterialTheme.colorScheme.onPrimary

                                    ui.billDoneCount > 0 ||
                                            ui.kitchenPendingCount > 0 ||
                                            ui.cartCount > 0 ->
                                        Color.White

                                    else ->
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                }

                                // STATUS — ONE CENTERED LABEL



                                val statusText = when {
                                    ui.billDoneCount > 0 && ui.cartCount > 0 ->
                                        "BILL ${ui.billDoneCount}\nCART ${ui.cartCount}"

                                    ui.billDoneCount > 0 ->
                                        "BILL ${ui.billDoneCount}"

                                    ui.kitchenPendingCount > 0 ->
                                        "KITCHEN"

                                    ui.cartCount > 0 ->
                                        "CART ${ui.cartCount}"

                                    else ->
                                        "AVAILABLE"
                                }




                                // TABLE CARD — SIMILAR TO TableChangeCard
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .animateContentSize()
                                        .clickable {
                                            onTableSelected(table.id)
                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = bgColor
                                    ),
                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = 3.dp
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // CENTERED TABLE NAME AND STATUS
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment =
                                                Alignment.CenterHorizontally,
                                            verticalArrangement =
                                                Arrangement.Center
                                        ) {
                                            Text(
                                                text = table.tableName,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = contentColor,
                                                textAlign = TextAlign.Center,
                                                maxLines = 2
                                            )

//                                            Spacer(
//                                                modifier = Modifier.height(1.dp)
//                                            )

                                            Text(
                                                text = statusText,
                                                fontSize = 12.sp,
                                                color = contentColor.copy(
                                                    alpha = 0.9f
                                                ),
                                                textAlign = TextAlign.Center
                                            )
//                                            Spacer(
//                                                modifier = Modifier.height(1.dp)
//                                            )
                                            if (ui.billAmount > 0.0) {
                                                Text(
                                                    text = "${String.format("%.2f", ui.billAmount)}",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF475569),
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1
                                                )
                                            }
                                        }

                                        // SELECTION CHECK — TOP RIGHT
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(2.dp),
                                                tint = contentColor
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
    }


    //PHONE

    if (isPhone) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                color = PosTheme.product.productCardBg,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 700.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(6.dp)
                ) {
                    // HEADER
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Dine in Tables",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = PosTheme.product.productCardText
                            )
                        )

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = PosTheme.product.productCardText
                            )
                        }
                    }

                    // TABLES GROUPED BY AREA
                    groupedByArea.entries.forEach { (areaName, areaTables) ->

                        Text(
                            text = areaName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PosTheme.product.productCardText
                            ),
                            modifier = Modifier
                                .padding(vertical = 5.dp)
                                .fillMaxWidth()
                        )

                        // MOBILE GRID DIMENSIONS
                        val screenWidth =
                            LocalConfiguration.current.screenWidthDp

                        val columns = (screenWidth / 110)
                            .coerceAtLeast(1)

                        val rows =
                            (areaTables.size + columns - 1) / columns

                        val gridHeight = (rows * 90).dp

                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 85.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(gridHeight)
                                .padding(bottom = 5.dp),
                            horizontalArrangement =
                                Arrangement.spacedBy(5.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(5.dp),
                            userScrollEnabled = false
                        ) {
                            items(areaTables) { ui ->

                                val table = ui.table
                                val isSelected = selectedTable == table.id

                                // SAME COLOR PRIORITY AS TABLET
                                val bgColor = when {
                                    isSelected ->
                                        Color(0xFF16A34A)

                                    ui.billDoneCount > 0 ->
                                        Color(0xFFE57373)

                                    ui.kitchenPendingCount > 0 ->
                                        Color(0xFF81C784)

                                    ui.cartCount > 0 ->
                                        Color(0xFF64B5F6)

                                    else ->
                                        MaterialTheme.colorScheme.surfaceVariant
                                }

                                val contentColor = when {
                                    isSelected ->
                                        MaterialTheme.colorScheme.onPrimary

                                    ui.billDoneCount > 0 ||
                                            ui.kitchenPendingCount > 0 ||
                                            ui.cartCount > 0 ->
                                        Color.White

                                    else ->
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                }

                                // SAME STATUS LOGIC AS TABLET
                                val statusText = when {
                                    ui.billDoneCount > 0 &&
                                            ui.cartCount > 0 ->
                                        "BILL ${ui.billDoneCount}\nCART ${ui.cartCount}"

                                    ui.billDoneCount > 0 ->
                                        "BILL ${ui.billDoneCount}"

                                    ui.kitchenPendingCount > 0 ->
                                        "KITCHEN"

                                    ui.cartCount > 0 ->
                                        "CART ${ui.cartCount}"

                                    else ->
                                        "AVAILABLE"
                                }

                                // MOBILE TABLE CARD
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(84.dp)
                                        .animateContentSize()
                                        .clickable {
                                            onTableSelected(table.id)
                                        },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = bgColor
                                    ),
                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = 2.dp
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(3.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // CENTERED NAME AND STATUS
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment =
                                                Alignment.CenterHorizontally,
                                            verticalArrangement =
                                                Arrangement.Center
                                        ) {
                                            Text(
                                                text = table.tableName,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = contentColor,
                                                textAlign = TextAlign.Center,
                                                maxLines = 2
                                            )

//                                            Spacer(
//                                                modifier = Modifier.height(1.dp)
//                                            )

                                            Text(
                                                text = statusText,
                                                fontSize = 13.sp,
                                                color = contentColor.copy(
                                                    alpha = 1f
                                                ),
                                                textAlign = TextAlign.Center,
                                                lineHeight = 13.sp
                                            )
//                                            Spacer(
//                                                modifier = Modifier.height(1.dp)
//                                            )

                                            if (ui.billAmount > 0.0) {
                                                Text(
                                                    text = "${String.format("%.2f", ui.billAmount)}",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF475569),
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1
                                                )
                                            }

                                        }

                                        // SELECTED CHECK
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .size(14.dp),
                                                tint = contentColor
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
    }


}




@Composable
fun StatusBadge(
    icon: String,
    text: String,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = icon,
            style = MaterialTheme.typography.bodySmall
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = PosTheme.product.productCardText
        )
    }
}


