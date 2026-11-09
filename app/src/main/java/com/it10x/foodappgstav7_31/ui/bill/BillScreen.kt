package com.it10x.foodappgstav7_31.ui.bill

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.it10x.foodappgstav7_31.ui.payment.PaymentType
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.lifecycle.viewmodel.compose.viewModel
import com.it10x.foodappgstav7_31.data.pos.entities.TableEntity
import com.it10x.foodappgstav7_31.ui.theme.PosTheme
import com.it10x.foodappgstav7_31.utils.formatter.MoneyFormatter
import com.it10x.foodappgstav7_31.viewmodel.PosTableViewModel

// =====================================================
// DELIVERY ADDRESS UI STATE (UI ONLY)
// =====================================================
data class DeliveryAddressUiState(
    val name: String = "",
    val phone: String = "",
    val line1: String = "",
    val line2: String = "",
    val city: String = "",
    val state: String = "",
    val zipcode: String = "",
    val landmark: String = ""
)

// =====================================================
// BILL SCREEN
// =====================================================

@Composable
fun BillScreen(
    viewModel: BillViewModel,
    currencyCode: String,
    localeTag: String,
    onPayClick: (PaymentType) -> Unit,
    onLastItemDelete: () -> Unit,
    onItemDelete: (String) -> Unit,
    tables: List<PosTableViewModel.TableUiState>,
    tableId: String?,
    selectedTableName: String,

) {
    val state by viewModel.uiState.collectAsState()
    val currency by viewModel.currencySymbol.collectAsState()
    val deliveryAddressState = remember {
        mutableStateOf(DeliveryAddressUiState())
    }

    var showAddressDialog by remember { mutableStateOf(false) }
    var pendingPaymentType by remember { mutableStateOf<PaymentType?>(null) }

    var showQtyDialog by remember { mutableStateOf(false) }
    var selectedItemId by remember { mutableStateOf<String?>(null) }
    var selectedItemQty by remember { mutableStateOf(0) }

  //  val event by viewModel.event.collectAsState()

    val context = LocalContext.current




    if (state.loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }


    LaunchedEffect(Unit) {
        viewModel.event.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .fillMaxHeight(0.95f)
//            .background(PosTheme.bill.billBg)
//            .padding(start = 6.dp, top = 6.dp, end = 6.dp)
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(PosTheme.bill.billBg)
            .padding(
                start = 6.dp,
                top = 6.dp,
                end = 6.dp
            )
    )
    {
        // 🔹 Fixed Header
        // 🔹 Scrollable Item List (takes all remaining space)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.items) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // LEFT: trash icon + name (name column takes remaining space)
                        Row(
                            modifier = Modifier
                                .weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        )
                        {
                            IconButton(
                                onClick = {
                                    if (state.items.size == 1) {
                                        onLastItemDelete()
                                    } else {
                                        onItemDelete(item.id)
                                       // viewModel.deleteItem(item.id)
                                    }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Item",
                                    tint = Color(0xFFD32F2F)
                                )
                            }



                            Column {

                                // 🔹 Item Name
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = PosTheme.bill.billText,
                                )

                                // 🔹 Modifiers
                                val modifiers = parseModifiers(item.modifiersJson)
                                modifiers.forEach { mod ->
                                    Text(
                                        text = "+ $mod",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PosTheme.bill.billText.copy(alpha = 0.75f)
                                    )
                                }

                                // 🔹 Note
                                if (item.note.isNotBlank()) {
                                    Text(
                                        text = item.note,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PosTheme.bill.billText.copy(alpha = 0.65f)
                                    )
                                }
                            }

                        }

                        // MIDDLE: qty + edit button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "x ${item.quantity}",
                                fontSize = 13.sp,
                                color = PosTheme.bill.billText
                            )
                            Spacer(Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = {
                                    selectedItemId = item.id
                                    selectedItemQty = item.quantity
                                    showQtyDialog = true
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Edit", fontSize = 12.sp)
                            }
                        }

                        // RIGHT: fixed-width aligned total (keeps alignment for different digits)
                        Text(
                            text = MoneyFormatter.format(
                                amount = item.itemtotal,
                                currencyCode = currencyCode,
                                localeTag = localeTag
                            ),
                            color = PosTheme.bill.billText,
                            modifier = Modifier.width(90.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }

        // 🔹 Fixed Footer (Totals)
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(
            color = PosTheme.bill.inputBorder
        )
        Spacer(Modifier.height(6.dp))

        BillRow(
            label = "Sub Total",
            value = state.subtotal,
            currencyCode = currencyCode,
            localeTag = localeTag
        )
        if (state.discountApplied > 0) {
             BillRow(
                "Discount",
                -state.discountApplied,
                currencyCode = currencyCode,
                localeTag = localeTag
            )
        }
        if (state.loyaltyDiscount > 0) {
            BillRow(
                label = "Loyalty Points Discount",
                value = -state.loyaltyDiscount,
                currencyCode = currencyCode,
                localeTag = localeTag
            )
        }
        BillRow("Tax", state.tax,  currencyCode = currencyCode,
            localeTag = localeTag)

// ✅ ADD HERE ↓↓↓
        if (state.deliveryFee > 0) {
            BillRow("Delivery", state.deliveryFee,  currencyCode = currencyCode,
                localeTag = localeTag)
        }

        if (state.deliveryTax > 0) {
            BillRow("Delivery Tax (5%)", state.deliveryTax,  currencyCode = currencyCode,
                localeTag = localeTag)
        }
// ✅ END

        BillRow("Grand Total", state.total,  currencyCode = currencyCode,
            localeTag = localeTag, bold = true)
    }


    // ---------- Quantity Edit Dialog ----------
    if (showQtyDialog && selectedItemId != null) {


        EditQuantityDialog(
            currentQty = selectedItemQty,
            itemName = state.items
                .find { it.id == selectedItemId }
                ?.name ?: "Selected Item",

            onDismiss = {
                showQtyDialog = false
            },

            onConfirm = { newQty, reason ->
                selectedItemId?.let { id ->

                    val otherItems = state.items.filter {
                        it.id != id
                    }

                    val hasOtherItems = otherItems.any {
                        it.quantity > 0
                    }

                    if (newQty == 0 && !hasOtherItems) {

                        // No other items remain: cancel the entire bill
                        viewModel.cancelBill(
                            payments = emptyList(),
                            name = "customer", // Replace with your actual state variable
                            phone = "1234", // Replace with your actual state variable
                            cancellationReason = reason
                        )

                    } else {

                        // Other items remain, or quantity is not zero
                        viewModel.updateItemQuantity(
                            itemId = id,
                            newQty = newQty,
                            reason = reason
                        )
                    }
                }

                showQtyDialog = false
            },

            // Actual table data for the Move Item dropdown
            tables = tables,

            // ID of the table currently being viewed
            currentTableId = tableId.orEmpty(),

            // Handle moving the selected item to another table
            onMoveItem = { qty, destinationId, destinationName ->

                val itemId = selectedItemId

                if (itemId != null) {
                    viewModel.moveItemQuantity(
                        itemId = itemId,
                        moveQty = qty,
                        destinationTableId = destinationId,
                        destinationTableName = destinationName
                    )
                }
            }
        )

    }
    // 🔐 Keep ViewModel updated

    LaunchedEffect(deliveryAddressState.value) {
        viewModel.setDeliveryAddress(deliveryAddressState.value)
    }
    // ---------------- ADDRESS DIALOG ----------------
    if (showAddressDialog) {
        DeliveryAddressDialog(
            addressState = deliveryAddressState,
            onDismiss = { showAddressDialog = false },
            onConfirm = {
                showAddressDialog = false
                pendingPaymentType?.let { onPayClick(it) }
                pendingPaymentType = null
            }
        )
    }
}

// =====================================================
// PAYMENT HANDLER
// =====================================================





// =====================================================
// VALIDATION
// =====================================================
private fun isAddressValid(
    addr: DeliveryAddressUiState,
    requireCity: Boolean = false,
    requireZip: Boolean = false
): Boolean {
    if (addr.phone.isBlank()) return false
    if (addr.line1.isBlank()) return false
    if (requireCity && addr.city.isBlank()) return false
    if (requireZip && addr.zipcode.isBlank()) return false
    return true
}

// =====================================================
// UI COMPONENTS
// =====================================================
@Composable
private fun BillRow(
    label: String,
    value: Double,
    currencyCode: String,
    localeTag: String,
    bold: Boolean = false
) {

    val formattedAmount = remember(value, currencyCode, localeTag) {
        MoneyFormatter.format(
            amount = value,
            currencyCode = currencyCode,
            localeTag = localeTag
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = label,
            color = PosTheme.bill.billText
        )

        Text(
            text = formattedAmount,
            color = PosTheme.bill.billText,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
    }
}



// =====================================================
// DELIVERY ADDRESS DIALOG (FIXED)
// =====================================================
@Composable
fun DeliveryAddressDialog(
    addressState: MutableState<DeliveryAddressUiState>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .imePadding(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                Text("Delivery Address", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))

                AddressField("Customer Name", addressState.value.name) {
                    addressState.value = addressState.value.copy(name = it)
                }

                AddressField("Phone", addressState.value.phone) {
                    addressState.value = addressState.value.copy(phone = it)
                }

                AddressField("Address Line 1", addressState.value.line1) {
                    addressState.value = addressState.value.copy(line1 = it)
                }

                AddressField("Address Line 2", addressState.value.line2) {
                    addressState.value = addressState.value.copy(line2 = it)
                }



                AddressField("Landmark", addressState.value.landmark) {
                    addressState.value = addressState.value.copy(landmark = it)
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (isAddressValid(addressState.value)) {
                                onConfirm()
                            }
                        }
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

@Composable
private fun AddressField(
    label: String,
    value: String,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}


fun parseModifiers(json: String): List<String> {
    return try {
        ModifierJsonHelper.fromJson(json)
            .flatMap { group ->
                group.items.map { item ->
                    "${item.name} (+${"%.2f".format(item.price)})"
                }
            }
    } catch (e: Exception) {
        emptyList()
    }
}


@Composable
fun EditQuantityDialog(
     currentQty: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit,
    itemName: String = "Selected Item",
    tables:  List<PosTableViewModel.TableUiState>,
    currentTableId: String,
     onMoveItem: (Int, String, String) -> Unit
) {
    var qty by remember(currentQty) {
        mutableStateOf(currentQty.coerceAtLeast(1))
    }

    var cancellationReason by remember {
        mutableStateOf("")
    }

    var moveQty by remember(currentQty) {
        mutableIntStateOf(1)
    }
    // Sample tables for UI design only.
    // Replace with actual table data later.
    val context = LocalContext.current

    val posTableViewModel: PosTableViewModel = viewModel()

    val tables by posTableViewModel.tables.collectAsState()

    LaunchedEffect(Unit) {
        posTableViewModel.loadTables()
    }

    var selectedTable by remember {
        mutableStateOf<String?>(null)
    }

    var selectedTableId by remember {
        mutableStateOf<String?>(null)
    }
    var expanded by remember {
        mutableStateOf(false)
    }



    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 440.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // =========================================
                // HEADER
                // =========================================

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LEFT: HEADER
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Edit Item",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = itemName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // RIGHT: CLOSE ICON
                    IconButton(
                        onClick = onDismiss
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close dialog"
                        )
                    }
                }



                HorizontalDivider()

                // =========================================
                // SECTION 1: CHANGE QUANTITY
                // =========================================

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "1",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.shapes.small
                                    )
                                    .padding(
                                        horizontal = 10.dp,
                                        vertical = 6.dp
                                    )
                            )

                            Column {
                                Text(
                                    text = "Change Quantity",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Text(
                                    text = "Adjust the number of portions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // DECREASE
                            FilledIconButton(
                                onClick = {
                                    if (qty > 0) qty--
                                },
                                enabled = qty > 0,
                                modifier = Modifier.size(44.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = Color(0xFFD32F2F),
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = "−",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // CURRENT QUANTITY
                            Column(
                                modifier = Modifier
                                    .width(72.dp)
                                    .padding(horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = qty.toString(),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = if (qty == 1) "portion" else "portions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // INCREASE
                            FilledIconButton(
                                onClick = {
                                    if (qty < 999) qty++
                                },
                                enabled = qty < 999,
                                modifier = Modifier.size(44.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = Color(0xFF388E3C),
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = "+",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // UPDATE BUTTON
                            Button(
                                onClick = {
                                    if (qty < currentQty && cancellationReason.isBlank()) {
                                        Toast.makeText(
                                            context,
                                            "Please enter a cancellation reason",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@Button
                                    }

                                    onConfirm(
                                        qty,
                                        if (qty < currentQty) cancellationReason.trim() else ""
                                    )
                                },
                                modifier = Modifier
                                    .width(90.dp)
                                    .height(44.dp),
                                contentPadding = PaddingValues(
                                    horizontal = 8.dp,
                                    vertical = 4.dp
                                )
                            ) {
                                Text(
                                    text = "Update",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    maxLines = 1
                                )
                            }
                        }

                        if (qty < currentQty) {
                            OutlinedTextField(
                                value = cancellationReason,
                                onValueChange = {
                                    cancellationReason = it
                                },
                                label = {
                                    Text("Cancellation reason")
                                },
                                placeholder = {
                                    Text("Why is this quantity being reduced?")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                maxLines = 3,
                                singleLine = false
                            )
                        }


                    }
                }

                // =========================================
                // SECTION 2: MOVE ITEM TO ANOTHER TABLE
                // =========================================

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "2",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.shapes.small
                                    )
                                    .padding(
                                        horizontal = 10.dp,
                                        vertical = 6.dp
                                    )
                            )

                            Column {
                                Text(
                                    text = "Move Item",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Text(
                                    text = "Transfer this item to another table",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = "Quantity to Move",
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedButton(
                                    enabled = moveQty > 1,
                                    onClick = {
                                        moveQty = (moveQty - 1).coerceAtLeast(1)
                                    }
                                ) {
                                    Text("−")
                                }

                                Text(
                                    text = "$moveQty / $currentQty",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                OutlinedButton(
                                    enabled = moveQty < currentQty,
                                    onClick = {
                                        moveQty = (moveQty + 1).coerceAtMost(currentQty)
                                    }
                                ) {
                                    Text("+")
                                }
                            }
                        }

                        Text(
                            text = "Destination Table",
                            style = MaterialTheme.typography.labelLarge
                        )

                        // DESTINATION TABLE SELECTOR
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    expanded = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(
                                    text = selectedTable
                                        ?: "Select destination table",
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Start
                                )

                                Text(
                                    text = if (expanded) "▲" else "▼",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = {
                                    expanded = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .heightIn(max = 300.dp)
                            ) {
                                val availableTables = tables.filter {
                                    it.table.id != currentTableId
                                }

                                availableTables.chunked(3).forEach { rowTables ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        rowTables.forEach { tableItem ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = tableItem.table.tableName,
                                                        textAlign = TextAlign.Center,
                                                        maxLines = 1,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                },
                                                onClick = {
                                                    selectedTable = tableItem.table.tableName
                                                    // Keep the destination ID separately for transfer logic.
                                                    selectedTableId = tableItem.table.id
                                                    expanded = false
                                                },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        repeat(3 - rowTables.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }

                        Text(
                            text = "Only the selected item will be moved. " +
                                    "Other items in the current order will remain unchanged.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedButton(
                            onClick = {
                                val destination = selectedTable

                                val destinationId = selectedTableId
                                val destinationName = selectedTable

                                if (
                                    destinationId == null ||
                                    destinationName == null ||
                                    currentQty <= 0
                                ) {
                                    Toast.makeText(
                                        context,
                                        "Select a destination table",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    onMoveItem(
                                        moveQty,
                                        destinationId,
                                        destinationName
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled = true,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                text = "Move Item",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // =========================================
                // FOOTER
                // =========================================

                HorizontalDivider()


            }
        }
    }
}





