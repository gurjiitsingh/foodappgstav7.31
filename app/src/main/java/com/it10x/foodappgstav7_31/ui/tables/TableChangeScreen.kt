package com.it10x.foodappgstav7_31.ui.tables

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.material3.Checkbox
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.it10x.foodappgstav7_31.data.pos.entities.TableEntity
import com.it10x.foodappgstav7_31.viewmodel.PosTableViewModel
import com.it10x.foodappgstav7_31.viewmodel.PosTableViewModel.TableColor
import androidx.compose.foundation.layout.BoxWithConstraints
@Composable
fun TableChangeScreen(
    navController: NavController,
    posTableViewModel: PosTableViewModel = viewModel(),
    onClose: () -> Unit
) {

    val tables by posTableViewModel.tables.collectAsState()
    val tableItems by posTableViewModel.tableItems.collectAsState()

    /*
     * ---------------------------------------------------------
     * STEP
     *
     * 0 = Select source table
     * 1 = Select operation
     * 2 = Select destination
     * ---------------------------------------------------------
     */
    var step by rememberSaveable {
        mutableIntStateOf(0)
    }

    /*
     * ---------------------------------------------------------
     * SELECTED SOURCE TABLE
     * ---------------------------------------------------------
     */
    var sourceTableId by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var selectedItemIds by rememberSaveable {
        mutableStateOf<Set<String>>(emptySet())
    }
    var migrationMode by rememberSaveable {
        mutableStateOf("FULL")
    }
    /*
     * ---------------------------------------------------------
     * SELECTED DESTINATION TABLE
     * ---------------------------------------------------------
     */
    var destinationTableId by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    /*
     * ---------------------------------------------------------
     * CONFIRMATION DIALOG
     * ---------------------------------------------------------
     */
    var showConfirmDialog by rememberSaveable {
        mutableStateOf(false)
    }

    /*
     * ---------------------------------------------------------
     * ONLY NORMAL DINE_IN TABLES
     *
     * TW / DL are virtual tables and must never appear here.
     * ---------------------------------------------------------
     */
    val dineInTables = remember(tables) {
        tables.filter { tableState ->

            val table = tableState.table

            table.id.startsWith("TW", ignoreCase = true).not() &&
                    table.id.startsWith("DL", ignoreCase = true).not()
        }
    }

    val sourceTable =
        dineInTables.firstOrNull {
            it.table.id == sourceTableId
        }?.table

    val destinationTable =
        dineInTables.firstOrNull {
            it.table.id == destinationTableId
        }?.table

    /*
     * ---------------------------------------------------------
     * SCREEN
     * ---------------------------------------------------------
     */
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        /*
         * =====================================================
         * TOP BAR
         * =====================================================
         */
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 4.dp
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = {

                        when (step) {

                            0 -> {
                                onClose()
                            }

                            1 -> {
                                step = 0
                            }

                            2 -> {
                                destinationTableId = null
                                step = 1
                            }
                        }
                    }
                ) {

                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text(
                    text = when (step) {

                        0 -> "Table Change"

                        1 -> "Table Change"

                        else ->
                            if (migrationMode == "FULL") {
                                "Migrate Full Table"
                            } else {
                                "Select Destination"
                            }
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        /*
         * =====================================================
         * STEP 0
         *
         * SELECT SOURCE TABLE
         * =====================================================
         */
        if (step == 0) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                Text(
                    text = "Select Table",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "Select the table you want to change.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                val groupedTables = dineInTables
                    .groupBy {
                        it.table.area
                            ?.trim()
                            ?.takeIf { area -> area.isNotBlank() }
                            ?: "Other"
                    }
                    .toSortedMap()

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    groupedTables.forEach { (areaName, areaTables) ->

                        // ==============================
                        // AREA TITLE
                        // ==============================

                        item(key = "area_$areaName") {

                            Text(
                                text = areaName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(
                                    horizontal = 4.dp,
                                    vertical = 4.dp
                                )
                            )
                        }

                        // ==============================
                        // TABLES FOR THIS AREA
                        // ==============================

                        item(key = "tables_$areaName") {

                            BoxWithConstraints(
                                modifier = Modifier.fillMaxWidth()
                            ) {

                                val minCellWidth = 110.dp
                                val spacing = 10.dp
                                val itemHeight = 90.dp

                                // Calculate actual number of columns
                                // based on available width.
                                val columns = maxOf(
                                    1,
                                    (
                                            (maxWidth.value + spacing.value) /
                                                    (minCellWidth.value + spacing.value)
                                            ).toInt()
                                )

                                // Calculate required rows for this area.
                                val rows =
                                    (areaTables.size + columns - 1) / columns

                                // Calculate exact grid height.
                                val gridHeight =
                                    (itemHeight * rows) +
                                            (
                                                    spacing *
                                                            (rows - 1)
                                                                .coerceAtLeast(0)
                                                    )

                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(columns),

                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(gridHeight),

                                    horizontalArrangement =
                                        Arrangement.spacedBy(spacing),

                                    verticalArrangement =
                                        Arrangement.spacedBy(spacing),

                                    userScrollEnabled = false
                                ) {

                                    gridItems(
                                        items = areaTables,
                                        key = {
                                            it.table.id
                                        }
                                    ) { tableState ->

                                        TableChangeCard(
                                            table = tableState.table,

                                            selected =
                                                tableState.table.id ==
                                                        sourceTableId,

                                            onClick = {

                                                sourceTableId =
                                                    tableState.table.id

                                                destinationTableId = null

                                                selectedItemIds =
                                                    emptySet()

                                                posTableViewModel
                                                    .loadTableItems(
                                                        tableState.table.id
                                                    )

                                                step = 1
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        /*
         * =====================================================
         * STEP 1
         *
         * SELECT OPERATION
         * =====================================================
         */

        if (step == 1) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Default.TableRestaurant,
                        contentDescription = null,
                        modifier = Modifier.size(42.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(
                        modifier = Modifier.width(12.dp)
                    )

                    Column {

                        Text(
                            text = sourceTable?.tableName
                                ?: "Selected Table",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Choose an action",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                /*
                 * -------------------------------------------------
                 * FULL TABLE
                 * -------------------------------------------------
                 */
                OperationCard(
                    title = "Migrate Full Table",
                    description = "Move the complete order to another table.",
                    enabled = true,
                    onClick = {

                        migrationMode = "FULL"

                        destinationTableId = null

                        step = 2
                    }
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                /*
                 * -------------------------------------------------
                 * SELECT ITEMS
                 * -------------------------------------------------
                 */
                Text(
                    text = "Select Items to Migrate",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "Select individual items from this table.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                if (tableItems.isEmpty()) {

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {

                            Text(
                                text = "No items found on this table.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                } else {

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        items(
                            items = tableItems,
                            key = {
                                it.id
                            }
                        ) { item ->

                            val selected =
                                selectedItemIds.contains(item.id)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {

                                        selectedItemIds =
                                            if (selected) {

                                                selectedItemIds - item.id

                                            } else {

                                                selectedItemIds + item.id
                                            }
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor =
                                        if (selected) {
                                            MaterialTheme.colorScheme
                                                .primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme
                                                .surfaceVariant
                                        }
                                )
                            ) {

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {

                                    androidx.compose.material3.Checkbox(
                                        checked = selected,
                                        onCheckedChange = {

                                            selectedItemIds =
                                                if (it) {
                                                    selectedItemIds + item.id
                                                } else {
                                                    selectedItemIds - item.id
                                                }
                                        }
                                    )

                                    Spacer(
                                        modifier = Modifier.width(8.dp)
                                    )

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {

                                        Text(
                                            text = item.name,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Spacer(
                                            modifier = Modifier.height(3.dp)
                                        )

                                        Text(
                                            text = "Qty: ${item.quantity}",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme
                                                .onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        text = "₹%.2f".format(
                                            item.finalPrice * item.quantity
                                        ),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                /*
                 * -------------------------------------------------
                 * SELECTED COUNT
                 * -------------------------------------------------
                 */
                Text(
                    text = "${selectedItemIds.size} item(s) selected",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                /*
                 * -------------------------------------------------
                 * NEXT
                 * -------------------------------------------------
                 *
                 * Destination selection will be step 2/next stage.
                 * -------------------------------------------------
                 */
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = selectedItemIds.isNotEmpty(),
                    onClick = {

                        /*
                         * For now we will use step 2 later
                         * for destination selection.
                         */
                        migrationMode = "ITEMS"

                        destinationTableId = null

                        step = 2
                    }
                ) {

                    Text(
                        text = "NEXT",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                OutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    onClick = {

                        selectedItemIds = emptySet()

                        posTableViewModel.clearTableItems()

                        step = 0
                    }
                ) {

                    Text("Cancel")
                }
            }
        }

        /*
         * =====================================================
         * STEP 2
         *
         * SELECT DESTINATION
         *
         * IMPORTANT:
         * ALL DINE_IN TABLES ARE DESTINATIONS.
         *
         * Occupied tables are intentionally NOT filtered out.
         *
         * Only the source table itself is disabled.
         * =====================================================
         */
        if (step == 2) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                /*
                 * SOURCE SUMMARY
                 */
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.primaryContainer
                    )
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(
                                text = "From",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme
                                    .onPrimaryContainer
                                    .copy(alpha = 0.7f)
                            )

                            Text(
                                text = sourceTable?.tableName
                                    ?: "-",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme
                                    .onPrimaryContainer
                            )
                        }

                        Text(
                            text = "→",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text = "Select Destination",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text =
                        if (migrationMode == "FULL") {
                            "Select the table to move this complete order to."
                        } else {
                            "Select the table to move the selected items to."
                        },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(110.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    gridItems(
                        items = dineInTables,
                        key = {
                            it.table.id
                        }
                    ) { tableState ->

                        val isSource =
                            tableState.table.id == sourceTableId

                        TableChangeCard(
                            table = tableState.table,
                            selected =
                                tableState.table.id ==
                                        destinationTableId,
                            enabled = !isSource,
                            onClick = {

                                if (isSource) {
                                    return@TableChangeCard
                                }

                                destinationTableId =
                                    tableState.table.id
                            }
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                /*
                 * SELECTED DESTINATION
                 */
                if (destinationTable != null) {

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {

                                Text(
                                    text = "Destination",
                                    fontSize = 13.sp
                                )

                                Text(
                                    text = destinationTable.tableName,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "${sourceTable?.tableName ?: "-"} → ${destinationTable.tableName}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )
                }

                /*
                 * MOVE BUTTON
                 */
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = destinationTableId != null,
                    onClick = {

                        val oldId =
                            sourceTableId
                                ?: return@Button

                        val newId =
                            destinationTableId
                                ?: return@Button

                        if (oldId == newId) {

                            Toast.makeText(
                                navController.context,
                                "Source and destination cannot be the same table",
                                Toast.LENGTH_SHORT
                            ).show()

                            return@Button
                        }

                        showConfirmDialog = true
                    }
                ) {

                    Text(
                        text = if (destinationTable != null) {
                            "MIGRATE ${sourceTable?.tableName ?: ""} → ${destinationTable.tableName}"
                        } else {
                            "MIGRATE TABLE"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    /*
     * =========================================================
     * CONFIRMATION
     * =========================================================
     */

    if (showConfirmDialog) {

        val oldTable = sourceTable

        val newTable = destinationTable

        AlertDialog(
            onDismissRequest = {
                showConfirmDialog = false
            },

            title = {
                Text(
                    text = "Confirm Table Migration",
                    fontWeight = FontWeight.Bold
                )
            },

            text = {

                Column {

                    Text(
                        text =
                            if (migrationMode == "FULL") {
                                "Move the complete order?"
                            } else {
                                "Move ${selectedItemIds.size} selected item(s)?"
                            }
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = oldTable?.tableName ?: "-",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.width(12.dp)
                        )

                        Text(
                            text = "→",
                            fontSize = 24.sp
                        )

                        Spacer(
                            modifier = Modifier.width(12.dp)
                        )

                        Text(
                            text = newTable?.tableName ?: "-",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Text(
                        text =
                            if (migrationMode == "FULL") {
                                "All items from ${oldTable?.tableName ?: "-"} " +
                                        "will be moved to ${newTable?.tableName ?: "-"}."
                            } else {
                                "${selectedItemIds.size} selected item(s) from " +
                                        "${oldTable?.tableName ?: "-"} will be moved to " +
                                        "${newTable?.tableName ?: "-"}."
                            },
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },

            confirmButton = {

                Button(
                    enabled =
                        oldTable != null &&
                                newTable != null,

                    onClick = {
                        val oldId =
                            sourceTableId
                                ?: return@Button

                        val newId =
                            destinationTableId
                                ?: return@Button

                        if (migrationMode == "FULL") {

                            posTableViewModel.transferTable(
                                oldTableId = oldId,
                                newTableId = newId
                            )

                        } else {

                            posTableViewModel.transferSelectedItems(
                                oldTableId = oldId,
                                newTableId = newId,
                                newTableName = newTable?.tableName ?: "",
                                itemIds = selectedItemIds.toList()
                            )
                        }

                        Toast.makeText(
                            navController.context,
                            if (migrationMode == "FULL") {
                                "Complete table moved to ${newTable?.tableName ?: ""}"
                            } else {
                                "${selectedItemIds.size} item(s) moved to ${newTable?.tableName ?: ""}"
                            },
                            Toast.LENGTH_SHORT
                        ).show()

                        showConfirmDialog = false

                        sourceTableId = null
                        destinationTableId = null
                        selectedItemIds = emptySet()

                        posTableViewModel.clearTableItems()

                        onClose()
                    }
                ) {

                    Text("CONFIRM")
                }
            },

            dismissButton = {

                OutlinedButton(
                    onClick = {
                        showConfirmDialog = false
                    }
                ) {

                    Text("CANCEL")
                }
            }
        )
    }


}

/*
 * =============================================================
 * TABLE CARD
 * =============================================================
 */
@Composable
private fun TableChangeCard(
    table: TableEntity,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {

    val tableColor = when {

        selected ->
            MaterialTheme.colorScheme.primary

        table.billCount > 0 ->
            Color(0xFFE57373)

        table.kitchenCount > 0 ->
            Color(0xFF81C784)

        table.cartCount > 0 ->
            Color(0xFF64B5F6)

        else ->
            MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when {

        selected ->
            MaterialTheme.colorScheme.onPrimary

        table.billCount > 0 ||
                table.kitchenCount > 0 ||
                table.cartCount > 0 ->
            Color.White

        else ->
            MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clickable(
                enabled = enabled,
                onClick = onClick
            ),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = tableColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (enabled) 3.dp else 0.dp
        )
    ) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = table.tableName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                val statusText = when {

                    table.billCount > 0 ->
                        "BILL"

                    table.kitchenCount > 0 ->
                        "KITCHEN"

                    table.cartCount > 0 ->
                        "${table.cartCount} ITEMS"

                    else ->
                        "AVAILABLE"
                }

                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    color = textColor.copy(alpha = 0.9f)
                )
            }

            if (selected) {

                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    tint = textColor
                )
            }
        }
    }
}


private fun calculateGridHeight(
    itemCount: Int,
    itemHeight: Dp,
    verticalSpacing: Dp
): Dp {
    val columns = 3
    val rows = (itemCount + columns - 1) / columns

    return (itemHeight * rows) +
            (verticalSpacing * (rows - 1).coerceAtLeast(0))
}

/*
 * =============================================================
 * OPERATION CARD
 * =============================================================
 */
@Composable
private fun OperationCard(
    title: String,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                onClick = onClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                if (enabled) {
                    MaterialTheme.colorScheme.surfaceVariant
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                        .copy(alpha = 0.45f)
                }
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        color =
                            if (enabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                                    .copy(alpha = 0.4f)
                            },
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = Icons.Default.TableRestaurant,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            Spacer(
                modifier = Modifier.width(14.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color =
                        if (enabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface
                                .copy(alpha = 0.45f)
                        }
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text = description,
                    fontSize = 13.sp,
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                            .copy(
                                alpha =
                                    if (enabled) 1f else 0.45f
                            )
                )
            }
        }
    }
}