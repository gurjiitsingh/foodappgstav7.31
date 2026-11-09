package com.it10x.foodappgstav7_31.ui.bill

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.it10x.foodappgstav7_31.ui.payment.PaymentInput
import com.it10x.foodappgstav7_31.ui.components.NumPad
import com.it10x.foodappgstav7_31.ui.pos.PosSessionViewModel
import com.it10x.foodappgstav7_31.ui.theme.PosTheme
import com.it10x.foodappgstav7_31.utils.MoneyUtils
import java.util.Locale
import com.it10x.foodappgstav7_31.utils.formatter.MoneyFormatter
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.input.KeyboardType
import com.it10x.foodappgstav7_31.viewmodel.PosTableViewModel

@Composable
fun BillDialog(
    showBill: Boolean,
    onDismiss: () -> Unit,
    sessionId: String?,
    tableId: String?,
    orderType: String,
    localeTag: String,
    currencyCode: String,
    selectedTableName: String,
    posSessionViewModel: PosSessionViewModel,
) {
    if (!showBill || sessionId == null) return
    val context = LocalContext.current

    val posTableViewModel: PosTableViewModel = viewModel()
    val tables by posTableViewModel.tables.collectAsState()

    LaunchedEffect(Unit) {
        posTableViewModel.loadTables()
    }

    //--------------- PHONE ---------------if (isProcessing) return@Buttonif (isProcessing) return@Buttonif (isProcessing) return@Button

    var activeInput by remember { mutableStateOf<String?>(null) }
    val discountFlat = remember { mutableStateOf("") }
    val discountPercent = remember { mutableStateOf("") }
    val creditAmount = remember { mutableStateOf("") }
    // Loyalty redemption input
    var redeemPointsInput by remember { mutableStateOf("") }

    val redeemPointsValue = redeemPointsInput.toLongOrNull() ?: 0L

// Default conversion: 100 points = ₹1.00
    val redeemDiscountPaise = redeemPointsValue

    val billViewModel: BillViewModel = viewModel(

        key = "BillVM_${sessionId}_${orderType}",
        factory = BillViewModelFactory(
            application = (LocalContext.current.applicationContext as? Application)
                ?: throw IllegalStateException("Application not found"),
            tableId = tableId ?: orderType,
            tableName = selectedTableName,
            orderType = orderType,
            posSessionViewModel = posSessionViewModel
        )
    )

    LaunchedEffect(redeemDiscountPaise) {
        billViewModel.setLoyaltyDiscountPaise(redeemDiscountPaise)
    }

    LaunchedEffect(redeemPointsValue) {
        billViewModel.setRedeemPoints(
            redeemPointsValue.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        )
    }

    val redeemDiscountText = String.format(
        java.util.Locale("en", "IN"),
        "₹%.2f",
        redeemDiscountPaise / 100.0
    )

    var showRemainingOptions by remember { mutableStateOf(false) }
    var showDiscount by remember { mutableStateOf(false) }
    var showDelivery by remember { mutableStateOf(false) }
    val deliveryFee = remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var receivedAmount by remember { mutableStateOf("") }
    val usedPaymentModes = remember { mutableStateListOf<String>() }
    var isCreditSelected by remember { mutableStateOf(false) }
   // val paymentList = remember { mutableStateListOf<PaymentInput>() }   // ✅ ADD THIS LINE


    // NEW PRINT WINDOW
    var isBillPrinted by remember { mutableStateOf(false) }
    var isPrinted by remember { mutableStateOf(false) }
    var showCancelReasonDialog by remember { mutableStateOf(false) }
    var cancellationReason by remember { mutableStateOf("") }

    var showItemDeleteReasonDialog by remember { mutableStateOf(false) }
    var itemDeleteReason by remember { mutableStateOf("") }
    var pendingDeleteItemId by remember { mutableStateOf<String?>(null) }




    LaunchedEffect(Unit) {
        billViewModel.toastEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(billViewModel.totalPaise) {
        receivedAmount = (billViewModel.totalPaise / 100.0).toString()
    }

    val uiState = billViewModel.uiState.collectAsState()

    val customerLoyaltyPoints by
    billViewModel.customerLoyaltyPoints.collectAsState()

    val allowDiscountPoints by
    billViewModel.allowPointsOnDiscountedBill.collectAsState()

    var hasLoadedItems by remember { mutableStateOf(false) }
    var showMoreOptions by remember { mutableStateOf(false) }
    var showComplimentaryReasons by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(uiState.value.items) {

        if (uiState.value.items.isNotEmpty()) {
            hasLoadedItems = true
        }

        if (hasLoadedItems && uiState.value.items.isEmpty()) {
            onDismiss()   // ✅ CLOSE DIALOG HERE
        }
    }

    val suggestions = billViewModel.customerSuggestions.collectAsState()
    val remainingPaise by billViewModel.remainingPaise.collectAsState()

    LaunchedEffect(showBill) {
        if (showBill) {

            if (uiState.value.discountFlat > 0) {
                discountFlat.value = uiState.value.discountFlat.toString()
                discountPercent.value = ""
                showDiscount = true
            }
            else if (uiState.value.discountPercent > 0) {
                discountPercent.value = uiState.value.discountPercent.toString()
                discountFlat.value = ""
                showDiscount = true
            }
            else {
                discountFlat.value = ""
                discountPercent.value = ""
            }
            // ✅ ADD THIS BLOCK
            if (uiState.value.deliveryFee > 0) {
                deliveryFee.value = uiState.value.deliveryFee.toString()
                showDelivery = true
            } else {
                deliveryFee.value = ""
                showDelivery = false
            }
        }
    }

    //ALERTE IS AMOUNT NOT EQUAL

    var showExcessAmountAlert by remember {
        mutableStateOf(false)
    }

    var pendingCashMode by remember {
        mutableStateOf<String?>(null)
    }

    var pendingCashAmount by remember {
        mutableStateOf<Long?>(null)
    }

    val receivedAmountValue = receivedAmount.toDoubleOrNull()
    val grandTotal = billViewModel.totalPaise / 100.0

//    LaunchedEffect(receivedAmount, billViewModel.totalPaise) {
//        showExcessAmountAlert =
//            receivedAmountValue != null &&
//                    receivedAmountValue > grandTotal
//    }

    var moreOptionsPhone by remember(sessionId) {
        mutableStateOf(uiState.value.customerPhone)
    }

    var showInsufficientAmountAlert by remember {
        mutableStateOf(false)
    }

    if (showExcessAmountAlert) {

        val excessAmount =
            ((receivedAmount.toDoubleOrNull() ?: 0.0) - grandTotal)
                .coerceAtLeast(0.0)

        AlertDialog(
            onDismissRequest = {
                showExcessAmountAlert = false
                pendingCashMode = null
                pendingCashAmount = null
            },
            title = {
                Text("Return Customer Excess Amount")
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Return the following excess cash to the customer:")

                    Text(
                        text = MoneyFormatter.format(
                            excessAmount,
                            currencyCode,
                            localeTag
                        ),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = PosTheme.bill.success
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val mode = pendingCashMode
                        val amount = pendingCashAmount

                        showExcessAmountAlert = false
                        pendingCashMode = null
                        pendingCashAmount = null

                        if (mode != null && amount != null) {
                            billViewModel.payBill(
                                payments = listOf(
                                    PaymentInput(
                                        mode = mode,
                                        amount = amount
                                    )
                                ),
                                name = "Customer",
                                phone = uiState.value.customerPhone
                            )
                        }
                    }
                ) {
                    Text("Confirm Cash Payment")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExcessAmountAlert = false
                        pendingCashMode = null
                        pendingCashAmount = null
                    }
                ) {
                    Text("Back")
                }
            }
        )
    }
    if (showInsufficientAmountAlert) {

        val entered = receivedAmount.toDoubleOrNull() ?: 0.0
        val balance = (grandTotal - entered).coerceAtLeast(0.0)

        AlertDialog(
            onDismissRequest = {
                showInsufficientAmountAlert = false
            },
            title = {
                Text("Amount Pending")
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("The received amount is less than the total payable.")

                    Text(
                        text = "Remaining amount: ${
                            MoneyFormatter.format(
                                balance,
                                currencyCode,
                                localeTag
                            )
                        }",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PosTheme.bill.warning
                    )

                    Text("Collect the remaining amount using another payment method.")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showInsufficientAmountAlert = false
                        showRemainingOptions = true
                    }
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showInsufficientAmountAlert = false
                    }
                ) {
                    Text("Back")
                }
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    )
    {

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            shape = MaterialTheme.shapes.medium,
            color = PosTheme.bill.billBg,
            contentColor = PosTheme.bill.billText,
            tonalElevation = 0.dp,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // ========= LEFT COLUMN (Bill List + Totals) =========
                Surface(
                    modifier = Modifier
                        .weight(1.6f)
                        .fillMaxHeight(),
                    color = PosTheme.bill.billTab,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(0.dp)
                    )
                    {


                        Text(
                            "Final Bill ${selectedTableName}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )


                        Divider(thickness = 1.dp, color = PosTheme.bill.billText.copy(alpha = 0.3f))
                        //**************************************************
                        // THIS IS BILL SCREEEN SHOW ITEMS
                        // **************************************************



                        BillScreen(
                            viewModel = billViewModel,
                            onPayClick = { paymentType ->

                                val totalAmount = billViewModel.totalPaise


                                billViewModel.payBill(
                                    payments = listOf(
                                        PaymentInput(
                                            mode = paymentType.name,
                                            amount = totalAmount
                                        )
                                    ),
                                    name = "Customer",
                                    phone = uiState.value.customerPhone
                                )

                                // onDismiss()
                            },
                            currencyCode = currencyCode,
                            localeTag = localeTag,
                            onLastItemDelete = {
                                cancellationReason = ""
                                showCancelReasonDialog = true
                            },
                            onItemDelete = { itemId ->
                                pendingDeleteItemId = itemId
                                itemDeleteReason = ""
                                showItemDeleteReasonDialog = true
                            },
                            tables = tables,
                            tableId = tableId,
                            selectedTableName = selectedTableName,
                        )
                    //}
                }
            }
                //**************************************************
                // ========= RIGHT COLUMN (Discount + Payment Buttons) =========
                // **************************************************

                Surface(
                    modifier = Modifier
                        .weight(1.4f)
                        .fillMaxHeight(),
                    color = PosTheme.bill.billBg,
                    shape = RoundedCornerShape(12.dp)
                )
                {
                    Column(
                        modifier = Modifier
                            .padding(vertical = 8.dp, horizontal = 6.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    )
                    {


// =====================================================
// ACTIONS — THREE ITEMS IN ONE ROW
// =====================================================

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            // -------- UNLOCK / NO PRINT --------
                            Button(
                                onClick = {
                                    isPrinted = !isPrinted
                                },
                                modifier = Modifier
                                    .height(28.dp)
                                    .width(82.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isPrinted)
                                        PosTheme.bill.warning
                                    else
                                        PosTheme.bill.success,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(
                                    horizontal = 4.dp,
                                    vertical = 0.dp
                                )
                            ) {
                                Text(
                                    text = if (isPrinted) "Unlock" else "No Print",
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }

                            // -------- MESSAGE --------
                            if (isPrinted) {
                                Text(
                                    text = "Complete payment or press Unlock.",
                                    color = PosTheme.bill.warning,
                                    fontSize = 10.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }

                            // -------- CLOSE --------
                            Button(
                                onClick = {
                                    if (!isPrinted) {
                                        onDismiss()
                                    }
                                },
                                enabled = !isPrinted,
                                modifier = Modifier
                                    .height(28.dp)
                                    .width(70.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PosTheme.bill.danger,
                                    contentColor = Color.White,
                                    disabledContainerColor =
                                        PosTheme.bill.danger.copy(alpha = 0.35f),
                                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                                ),
                                contentPadding = PaddingValues(
                                    horizontal = 4.dp,
                                    vertical = 0.dp
                                )
                            ) {
                                Text(
                                    text = "Close",
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                        }


                        // =====================================================
// CUSTOMER PHONE + LOYALTY POINTS
// =====================================================

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        )
                        {

                            // Customer Phone
                            Box(
                                modifier = Modifier
                                    .weight(0.40f)
                                    .clickable { activeInput = "PHONE" }
                            ) {
                                OutlinedTextField(
                                    value = uiState.value.customerPhone,
                                    onValueChange = {},
                                    label = { Text("Customer Phone") },
                                    enabled = false,
                                    readOnly = true,
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledContainerColor =
                                            if (activeInput == "PHONE")
                                                PosTheme.bill.inputActiveBg
                                            else
                                                PosTheme.bill.inputBg,
                                        disabledBorderColor =
                                            if (activeInput == "PHONE")
                                                PosTheme.bill.inputActiveBorder
                                            else
                                                PosTheme.bill.inputBorder,
                                        disabledTextColor = PosTheme.bill.billText,
                                        disabledLabelColor =
                                            PosTheme.bill.billText.copy(alpha = 0.7f)
                                    ),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 15.sp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 56.dp)
                                )
                            }




                            // Allow / Disallow Points Button
                            Button(
                                onClick = {
                                    billViewModel.setAllowPointsOnDiscountedBill(
                                        !allowDiscountPoints
                                    )
                                },
                                modifier = Modifier
                                    .height(42.dp)
                                    .weight(0.22f),
                                contentPadding = PaddingValues(
                                    horizontal = 4.dp,
                                    vertical = 0.dp
                                ),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (allowDiscountPoints)
                                        PosTheme.bill.success
                                    else
                                        PosTheme.bill.warning,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = if (allowDiscountPoints) "Earn ON" else "Earn OFF",
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }

                            // Loyalty Points
                            OutlinedTextField(
                                value = "$customerLoyaltyPoints Points",
                                onValueChange = {},
                                readOnly = true,
                                enabled = false,
                                label = { Text("Loyalty Points") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledContainerColor = PosTheme.bill.inputBg,
                                    disabledBorderColor = PosTheme.bill.inputBorder,
                                    disabledTextColor = PosTheme.bill.success,
                                    disabledLabelColor =
                                        PosTheme.bill.billText.copy(alpha = 0.7f)
                                ),
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier
                                    .weight(0.38f)
                                    .heightIn(min = 56.dp)
                            )
                        }

                        // =====================================================
// LOYALTY POINT REDEMPTION
// =====================================================

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        )
                        {

                            // Redeem Points Input
                            // Redeem Points Input
                            Box(
                                modifier = Modifier
                                    .weight(0.50f)
                                    .clickable {
                                        activeInput = "REDEEM_POINTS"
                                    }
                            ) {
                                OutlinedTextField(
                                    value = redeemPointsInput,
                                    onValueChange = {},
                                    readOnly = true,
                                    enabled = false,
                                    label = { Text("Redeem Points") },
                                    singleLine = true,
                                    textStyle = LocalTextStyle.current.copy(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledContainerColor = if (activeInput == "REDEEM_POINTS")
                                            PosTheme.bill.inputActiveBg
                                        else
                                            PosTheme.bill.inputBg,
                                        disabledBorderColor = if (activeInput == "REDEEM_POINTS")
                                            PosTheme.bill.inputActiveBorder
                                        else
                                            PosTheme.bill.inputBorder,
                                        disabledTextColor = PosTheme.bill.billText,
                                        disabledLabelColor = PosTheme.bill.billText.copy(alpha = 0.7f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 56.dp)
                                )
                            }

                            // Redemption Discount Display
                            OutlinedTextField(
                                value = redeemDiscountText,
                                onValueChange = {},
                                readOnly = true,
                                enabled = false,
                                label = { Text("Discount") },
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledContainerColor = PosTheme.bill.inputBg,
                                    disabledBorderColor = PosTheme.bill.inputBorder,
                                    disabledTextColor = PosTheme.bill.success,
                                    disabledLabelColor = PosTheme.bill.billText.copy(alpha = 0.7f)
                                ),
                                modifier = Modifier
                                    .weight(0.50f)
                                    .heightIn(min = 56.dp)
                            )
                        }

                    if (suggestions.value.isNotEmpty() && activeInput == "PHONE") {

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = PosTheme.bill.inputBg
                            )
                        ) {
                            Column {
                                suggestions.value.forEach { customer ->

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                billViewModel.setCustomerPhone(customer.phone)
                                                billViewModel.clearCustomerSuggestions()
                                                activeInput = null
                                            }
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = "${customer.phone}  (${customer.name})",
                                            color = PosTheme.bill.billText,
                                            fontSize = 14.sp
                                        )
                                    }

                                    Divider(color = PosTheme.bill.billText.copy(alpha = 0.15f))
                                }
                            }
                        }
                    }


// =====================================================
// DISCOUNT — SINGLE ROW
// =====================================================

                        Log.d("ISPIRINTED", "is print done = ${isPrinted}")

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {

                            // -------- ENABLE DISCOUNT --------
                            Text(
                                text = "Discount",
                                fontSize = 12.sp,
                                color = PosTheme.bill.billText.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )

                            Switch(
                                checked = showDiscount,
                                onCheckedChange = { showDiscount = it },
                                modifier = Modifier.scale(0.75f),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PosTheme.bill.warning,
                                    checkedTrackColor = PosTheme.bill.warning.copy(alpha = 0.35f),
                                    uncheckedThumbColor = PosTheme.bill.billText.copy(alpha = 0.6f),
                                    uncheckedTrackColor = PosTheme.bill.inputBorder.copy(alpha = 0.35f),
                                    uncheckedBorderColor = PosTheme.bill.inputBorder
                                )
                            )

                            if (showDiscount) {

                                // -------- CLEAR DISCOUNT --------
                                TextButton(
                                    onClick = {
                                        if (!isPrinted) {
                                            discountFlat.value = ""
                                            discountPercent.value = ""

                                            billViewModel.setFlatDiscount(0.0)
                                            billViewModel.setPercentDiscount(0.0)

                                            activeInput = null
                                        }
                                    },
                                    enabled = !isPrinted,
                                    contentPadding = PaddingValues(horizontal = 2.dp),
                                    modifier = Modifier.widthIn(min = 28.dp)
                                ) {
                                    Text(
                                        text = "❌",
                                        fontSize = 12.sp
                                    )
                                }
                                // -------- FLAT DISCOUNT --------
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            activeInput = "FLAT"
                                            discountPercent.value = ""
                                            billViewModel.setPercentDiscount(0.0)
                                        }
                                ) {
                                    OutlinedTextField(
                                        value = discountFlat.value,
                                        onValueChange = {},
                                        label = { Text("Flat", fontSize = 11.sp) },
                                        readOnly = true,
                                        enabled = isPrinted,
                                        singleLine = true,
                                        textStyle = LocalTextStyle.current.copy(
                                            fontSize = 12.sp
                                        ),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            disabledContainerColor =
                                                if (activeInput == "FLAT")
                                                    PosTheme.bill.inputActiveBg
                                                else
                                                    PosTheme.bill.inputBg,

                                            disabledBorderColor =
                                                if (activeInput == "FLAT")
                                                    PosTheme.bill.inputActiveBorder
                                                else
                                                    PosTheme.bill.inputBorder,

                                            disabledTextColor = PosTheme.bill.billText,
                                            disabledLabelColor =
                                                PosTheme.bill.billText.copy(alpha = 0.7f)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                // -------- PERCENT DISCOUNT --------


                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            activeInput = "PERCENT"
                                            discountFlat.value = ""
                                            billViewModel.setFlatDiscount(0.0)
                                        }
                                ) {
                                    OutlinedTextField(
                                        value = discountPercent.value,
                                        onValueChange = {},
                                        label = { Text("%", fontSize = 11.sp) },
                                        readOnly = true,
                                        enabled = isPrinted,
                                        singleLine = true,
                                        textStyle = LocalTextStyle.current.copy(
                                            fontSize = 12.sp
                                        ),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            disabledContainerColor =
                                                if (activeInput == "PERCENT")
                                                    PosTheme.bill.inputActiveBg
                                                else
                                                    PosTheme.bill.inputBg,

                                            disabledBorderColor =
                                                if (activeInput == "PERCENT")
                                                    PosTheme.bill.inputActiveBorder
                                                else
                                                    PosTheme.bill.inputBorder,

                                            disabledTextColor = PosTheme.bill.billText,
                                            disabledLabelColor =
                                                PosTheme.bill.billText.copy(alpha = 0.7f)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }


                            }
                        }

// =====================================================
// DELIVERY CHARGES — SINGLE ROW
// =====================================================

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {

                            // -------- DELIVERY TOGGLE --------
                            Text(
                                text = "Delivery Fee",
                                fontSize = 12.sp,
                                color = PosTheme.bill.billText.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )

                            Switch(
                                checked = showDelivery,
                                onCheckedChange = { showDelivery = it },
                                modifier = Modifier.scale(0.75f),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PosTheme.bill.warning,
                                    checkedTrackColor = PosTheme.bill.warning.copy(alpha = 0.35f),
                                    uncheckedThumbColor = PosTheme.bill.billText.copy(alpha = 0.6f),
                                    uncheckedTrackColor = PosTheme.bill.inputBorder.copy(alpha = 0.35f),
                                    uncheckedBorderColor = PosTheme.bill.inputBorder
                                )
                            )

                            // -------- DELIVERY INPUT --------
                            if (showDelivery) {

                                // -------- CLEAR DELIVERY FEE --------
                                TextButton(
                                    onClick = {
                                        if (!isPrinted) {
                                            deliveryFee.value = ""
                                            activeInput = null
                                            billViewModel.setDeliveryFee(0.0)
                                        }
                                    },
                                    enabled = !isPrinted,
                                    contentPadding = PaddingValues(horizontal = 2.dp),
                                    modifier = Modifier.widthIn(min = 28.dp)
                                ) {
                                    Text(
                                        text = "❌",
                                        fontSize = 12.sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            activeInput = "DELIVERY"
                                        }
                                ) {
                                    OutlinedTextField(
                                        value = deliveryFee.value,
                                        onValueChange = {},
                                        label = {
                                            Text(
                                                text = "Delivery Fee",
                                                fontSize = 11.sp
                                            )
                                        },
                                        readOnly = true,
                                        enabled = isPrinted,
                                        singleLine = true,
                                        textStyle = LocalTextStyle.current.copy(
                                            fontSize = 12.sp
                                        ),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            disabledContainerColor =
                                                if (activeInput == "DELIVERY")
                                                    PosTheme.bill.inputActiveBg
                                                else
                                                    PosTheme.bill.inputBg,

                                            disabledBorderColor =
                                                if (activeInput == "DELIVERY")
                                                    PosTheme.bill.inputActiveBorder
                                                else
                                                    PosTheme.bill.inputBorder,

                                            disabledTextColor = PosTheme.bill.billText,
                                            disabledLabelColor =
                                                PosTheme.bill.billText.copy(alpha = 0.7f)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }


                            }
                        }


// ==========================================
// AMOUNT RECEIVED INPUT
// ==========================================

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Payable: ${
                                    MoneyFormatter.format(
                                        billViewModel.totalPaise.toDouble()/ 100.0,
                                        currencyCode,
                                        localeTag
                                    )
                                }",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PosTheme.bill.billText,
                                modifier = Modifier.weight(1f)
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(
                                        interactionSource = remember {
                                            MutableInteractionSource()
                                        },
                                        indication = null
                                    ) {
                                        activeInput = "RECEIVED_AMOUNT"
                                    }
                            ) {
                                OutlinedTextField(
                                    value = receivedAmount,
                                    onValueChange = {},
                                    label = { Text("Amount Received") },
                                    placeholder = { Text("Enter amount") },
                                    readOnly = true,
                                    enabled = false,
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledContainerColor = PosTheme.bill.inputBg,
                                        disabledBorderColor = PosTheme.bill.inputBorder,
                                        disabledTextColor = PosTheme.bill.billText,
                                        disabledLabelColor = PosTheme.bill.billText,
                                        disabledPlaceholderColor = PosTheme.bill.billText
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }


                        // ---------- PAYMENT BUTTONS (Compact, Pastel Colors) ----------
//                    Text("Select Payment", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(4.dp))



                             val showPrintButton = !isPrinted;
                              if (showPrintButton) {

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                PrintButton(
                                    modifier = Modifier.weight(1f),
                                    onPrint = {
                                        billViewModel.printCurrentBill()
                                        isPrinted = true
                                    }
                                )

                                CancelButton(
                                    modifier = Modifier.weight(1f),
                                    text = "Cancel Bill",
                                   // color = Color(0xFF2E7D32),
                                    onClick = {
                                        cancellationReason = ""
                                        showCancelReasonDialog = true
                                    }
                                )
                            }
                        }

                    else {
                        PaymentButtonsSection(
                            remainingPaise = remainingPaise,

//                            onPay = { mode, amount ->
//                                val finalPayments = listOf(
//                                    PaymentInput(
//                                        mode = mode,
//                                        amount = amount
//                                    )
//                                )
//
//                                billViewModel.payBill(
//                                    payments = finalPayments,
//                                    name = "Customer",
//                                    phone = uiState.value.customerPhone
//                                )
//                            },
                            onPay = { mode, amount ->

                                val enteredAmount = receivedAmount.toDoubleOrNull()
                                val total = billViewModel.totalPaise / 100.0

                                if (mode.equals("CASH", ignoreCase = true)) {

                                    when {
                                        enteredAmount == null -> {
                                            Toast.makeText(
                                                context,
                                                "Please enter the amount received",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                        enteredAmount > total -> {
                                            pendingCashMode = mode
                                            pendingCashAmount = amount
                                            showExcessAmountAlert = true
                                        }

                                        enteredAmount < total -> {
                                            showInsufficientAmountAlert = true
                                        }

                                        else -> {
                                            billViewModel.payBill(
                                                payments = listOf(
                                                    PaymentInput(
                                                        mode = mode,
                                                        amount = amount
                                                    )
                                                ),
                                                name = "Customer",
                                                phone = uiState.value.customerPhone
                                            )
                                        }
                                    }

                                    return@PaymentButtonsSection
                                }

                                // Keep existing behavior for other payment methods
                                billViewModel.payBill(
                                    payments = listOf(
                                        PaymentInput(
                                            mode = mode,
                                            amount = amount
                                        )
                                    ),
                                    name = "Customer",
                                    phone = uiState.value.customerPhone
                                )
                            },

                            onMoreClick = {
                                moreOptionsPhone = uiState.value.customerPhone
                                showMoreOptions = true
                            },

                            // ✅ NEW PARAMS
                            currencyCode = currencyCode,
                            localeTag = localeTag,

                            isCreditSelected = isCreditSelected,
                            showRemainingOptions = showRemainingOptions,

                            onCreditClick = {
                                billViewModel.clearCredit()

                                val paise = remainingPaise
                                val rupees = paise / 100
                                val paisaPart = paise % 100

                                creditAmount.value = if (paisaPart == 0L) {
                                    rupees.toString()
                                } else {
                                    "$rupees.${paisaPart.toString().padStart(2, '0')}"
                                }

                                activeInput = "CREDIT"
                                isCreditSelected = true

                                // Do not set showMoreOptions = false here
                            },

                            onPayLaterClick = {
                                val phone = uiState.value.customerPhone.trim()

                                if (phone.length != 10) {
                                    Toast.makeText(context, "Enter valid 10 digit phone number", Toast.LENGTH_SHORT).show()
                                    return@PaymentButtonsSection
                                }

                                billViewModel.payBill(
                                    payments = listOf(
                                        PaymentInput("DELIVERY_PENDING", remainingPaise)
                                    ),
                                    name = "Customer",
                                    phone = phone
                                )
                            },

                            // ✅ CREDIT UI PASSED HERE
                            creditContent = {

                                Column {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {

                                        OutlinedTextField(
                                            value = creditAmount.value,
                                            onValueChange = {},
                                            label = { Text("Credit") },
                                            readOnly = true,
                                            modifier = Modifier.weight(1f)
                                        )


                                        IconButton(onClick = {
                                            creditAmount.value = ""
                                            activeInput = null
                                            isCreditSelected = false
                                        }) {
                                            Text("✕")
                                        }

                                        // ✔ Confirm
                                        IconButton(onClick = {

                                            val input = creditAmount.value.trim()
                                            val parts = input.split(".")

                                            val rupees = parts.getOrNull(0)?.toLongOrNull() ?: 0L
                                            val paise = parts.getOrNull(1)?.padEnd(2, '0')?.take(2)?.toLongOrNull() ?: 0L
                                            val enteredPaise = rupees * 100 + paise

                                            if (enteredPaise <= 0 || enteredPaise > remainingPaise) return@IconButton

                                            billViewModel.setCreditAmountRaw(input)

                                            val totalPaise = billViewModel.totalPaise

                                            if (enteredPaise == totalPaise) {
                                                billViewModel.payBill(
                                                    payments = emptyList(),
                                                    name = "Customer",
                                                    phone = uiState.value.customerPhone
                                                )
                                            } else {
                                                showRemainingOptions = true
                                            }

                                            creditAmount.value = ""
                                            activeInput = null
                                            isCreditSelected = false

                                        }) {
                                            Text("✔")
                                        }
                                    }
                                }
                            }
                        )

                }

                        if (showCancelReasonDialog) {
                            AlertDialog(
                                onDismissRequest = {
                                    showCancelReasonDialog = false
                                },
                                title = {
                                    Text("Cancel Bill")
                                },
                                text = {
                                    OutlinedTextField(
                                        value = cancellationReason,
                                        onValueChange = {
                                            cancellationReason = it
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = {
                                            Text("Reason")
                                        },
                                        placeholder = {
                                            Text("Enter cancellation reason")
                                        },
                                        singleLine = false,
                                        minLines = 3,
                                        maxLines = 5
                                    )
                                },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            val reason = cancellationReason.trim()

                                            if (reason.isBlank()) {
                                                return@TextButton
                                            }

                                            val finalPayments = listOf(
                                                PaymentInput(
                                                    mode = "CANCEL",
                                                    amount = remainingPaise
                                                )
                                            )

                                            showCancelReasonDialog = false

                                            billViewModel.cancelBill(
                                                payments = finalPayments,
                                                name = "Customer",
                                                phone = uiState.value.customerPhone,
                                                cancellationReason = reason
                                            )
                                        }
                                    ) {
                                        Text("Cancel Bill")
                                    }
                                },
                                dismissButton = {
                                    TextButton(
                                        onClick = {
                                            showCancelReasonDialog = false
                                        }
                                    ) {
                                        Text("Close")
                                    }
                                }
                            )
                        }

                        if (showItemDeleteReasonDialog) {
                            AlertDialog(
                                onDismissRequest = {
                                    showItemDeleteReasonDialog = false
                                },
                                title = {
                                    Text("Delete Item")
                                },
                                text = {
                                    OutlinedTextField(
                                        value = itemDeleteReason,
                                        onValueChange = {
                                            itemDeleteReason = it
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = {
                                            Text("Reason")
                                        },
                                        placeholder = {
                                            Text("Enter reason for deleting item")
                                        },
                                        minLines = 3,
                                        maxLines = 5
                                    )
                                },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            val reason = itemDeleteReason.trim()

                                            if (reason.isBlank()) {
                                                return@TextButton
                                            }

                                            pendingDeleteItemId?.let { itemId,  ->
                                                billViewModel.deleteItem(itemId, reason = reason)
                                            }

                                            showItemDeleteReasonDialog = false
                                            pendingDeleteItemId = null
                                            itemDeleteReason = ""
                                        }
                                    ) {
                                        Text("Delete Item")
                                    }
                                },
                                dismissButton = {
                                    TextButton(
                                        onClick = {
                                            showItemDeleteReasonDialog = false
                                            pendingDeleteItemId = null
                                        }
                                    ) {
                                        Text("Close")
                                    }
                                }
                            )
                        }

// ===============================
// GLOBAL NUMPAD (Single Keyboard)
// ===============================



                        Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(
                        color = PosTheme.bill.inputBorder
                    )
                        Spacer(modifier = Modifier.height(8.dp))

                    NumPad { label ->
                        handleInput(
                            label = label,
                            activeInput = activeInput,
                            uiState = uiState.value,
                            discountFlat = discountFlat,
                            discountPercent = discountPercent,
                            creditAmount = creditAmount,
                            deliveryFee = deliveryFee,
                            receivedAmount = receivedAmount,
                            setReceivedAmount = { receivedAmount = it },
                            redeemPointsInput = redeemPointsInput,
                            setRedeemPointsInput = { redeemPointsInput = it },
                            billViewModel = billViewModel,
                            isPrinted = isPrinted
                        )
                    }











                }

            }}
        }
    }


    if (showMoreOptions) {

        MorePaymentOptionsDialog(
            onDismiss = {
                showMoreOptions = false
            },

            onCreditClick = {
                billViewModel.clearCredit()

                val paise = remainingPaise
                val rupees = paise / 100
                val paisaPart = paise % 100

                creditAmount.value = if (paisaPart == 0L) {
                    rupees.toString()
                } else {
                    "$rupees.${paisaPart.toString().padStart(2, '0')}"
                }

                activeInput = "CREDIT"
                isCreditSelected = true
                showRemainingOptions = false
            },

            onPayLaterClick = {
                val phone = moreOptionsPhone.trim()

                if (phone.length != 10 || !phone.all { it.isDigit() }) {
                    Toast.makeText(
                        context,
                        "Enter valid 10 digit phone number",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    billViewModel.setCustomerPhone(phone)

                    billViewModel.payBill(
                        payments = listOf(
                            PaymentInput("DELIVERY_PENDING", remainingPaise)
                        ),
                        name = "Customer",
                        phone = phone
                    )

                    showMoreOptions = false
                }
            },

            onComplimentaryClick = {
                showMoreOptions = false
                showComplimentaryReasons = true
            },

            showCreditInput = isCreditSelected,
            creditAmount = creditAmount.value,

            onCreditCancel = {
                creditAmount.value = ""
                activeInput = null
                isCreditSelected = false
            },

            onCreditConfirm = {
                val phone = moreOptionsPhone.trim()

                if (phone.length != 10 || !phone.all { it.isDigit() }) {
                    Toast.makeText(
                        context,
                        "Enter valid 10 digit phone number",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@MorePaymentOptionsDialog
                }

                val input = creditAmount.value.trim()
                val parts = input.split(".")

                val rupees = parts.getOrNull(0)?.toLongOrNull() ?: 0L
                val paise = parts.getOrNull(1)
                    ?.padEnd(2, '0')
                    ?.take(2)
                    ?.toLongOrNull() ?: 0L

                val enteredPaise = rupees * 100 + paise

                if (enteredPaise <= 0L || enteredPaise > remainingPaise) {
                    Toast.makeText(
                        context,
                        "Enter a valid credit amount",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@MorePaymentOptionsDialog
                }

                billViewModel.setCustomerPhone(phone)
                billViewModel.setCreditAmountRaw(input)

                val totalPaise = billViewModel.totalPaise

                if (enteredPaise == totalPaise) {
                    // Full bill on credit.
                    billViewModel.payBill(
                        payments = emptyList(),
                        name = "Customer",
                        phone = phone
                    )

                    showMoreOptions = false
                    showRemainingOptions = false
                } else {
                    // Partial credit: display the remaining payment options
                    // inside MorePaymentOptionsDialog.
                    showRemainingOptions = true
                }

                creditAmount.value = ""
                activeInput = null
                isCreditSelected = false
            },

            onComplimentaryReasonClick = { reason ->
                showMoreOptions = false

                // Keep your existing complimentary-reason saving logic here.
            },

            phoneNumber = moreOptionsPhone,

            onPhoneNumberChange = { value ->
                moreOptionsPhone = value
            },

            onPhoneNumberConfirm = {
                val phone = moreOptionsPhone.trim()

                if (phone.length != 10 || !phone.all { it.isDigit() }) {
                    Toast.makeText(
                        context,
                        "Enter valid 10 digit phone number",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    billViewModel.setCustomerPhone(phone)

                    Toast.makeText(
                        context,
                        "Phone number saved",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },

            onCreditAmountChange = { value ->
                creditAmount.value = value
            },

            // NEW: show the remaining-payment options inside the dialog.
            showRemainingOptions = showRemainingOptions,
            remainingPaise = remainingPaise,

            // NEW: handle Cash, Card or UPI for the remaining amount.
            onPayRemaining = { paymentType, amountPaise ->
                billViewModel.payBill(
                    payments = listOf(
                        PaymentInput(
                            mode = paymentType,
                            amount = amountPaise
                        )
                    ),
                    name = "Customer",
                    phone = moreOptionsPhone.trim()
                )

                showMoreOptions = false
                showRemainingOptions = false
            },

            onConfirmMixedPayments = { payments ->

                val phone = moreOptionsPhone.trim()

//                if (phone.length != 10 || !phone.all { it.isDigit() }) {
//                    Toast.makeText(
//                        context,
//                        "Enter valid 10 digit phone number",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                } else {
                    billViewModel.payBill(
                        payments = payments,
                        name = "Customer",
                        phone = phone
                    )

                    showMoreOptions = false
                    showRemainingOptions = false
              //  }
            }
        )

    }





}//end of main funciton



fun handleInput(
    label: String,
    activeInput: String?,
    uiState: BillUiState,
    discountFlat: MutableState<String>,
    discountPercent: MutableState<String>,
    creditAmount: MutableState<String>,
    deliveryFee: MutableState<String>,
    receivedAmount: String,
    setReceivedAmount: (String) -> Unit,
    redeemPointsInput: String,
    setRedeemPointsInput: (String) -> Unit,
    billViewModel: BillViewModel,
    isPrinted: Boolean   // ✅ NEW PARAM
)
 {

    // 🛑 GLOBAL LOCK (after print)
//    if (isPrinted) {
//        // ✅ Allow ONLY phone editing (optional)
//        if (activeInput != "PHONE") return
//    }
    if (isPrinted) {
        if (
            activeInput != "PHONE" &&
            activeInput != "RECEIVED_AMOUNT" &&
            activeInput != "DELIVERY"
        ) return




    }

    when (activeInput) {

        // =========================
        // 📱 PHONE INPUT
        // =========================
        "PHONE" -> {
            when (label) {

                "←" -> {
                    if (uiState.customerPhone.isNotEmpty()) {

                        val newPhone = uiState.customerPhone.dropLast(1)
                        billViewModel.setCustomerPhone(newPhone)

                        if (newPhone.length in 3..9) {
                            billViewModel.observeCustomerSuggestions(newPhone)
                        } else {
                            billViewModel.clearCustomerSuggestions()
                        }
                    }
                }

                "." -> {
                    // ignore dot
                }

                else -> {
                    if (uiState.customerPhone.length < 10) {

                        val newPhone = uiState.customerPhone + label
                        billViewModel.setCustomerPhone(newPhone)

                        if (newPhone.length in 3..9) {
                            billViewModel.observeCustomerSuggestions(newPhone)
                        } else {
                            billViewModel.clearCustomerSuggestions()
                        }
                    }
                }
            }
        }

        // =========================
        // 💰 FLAT DISCOUNT
        // =========================
        "FLAT" -> {
            discountFlat.value = handleNumberInput(discountFlat.value, label)

            billViewModel.setFlatDiscount(
                discountFlat.value.toDoubleOrNull() ?: 0.0
            )
        }

        // =========================
        // 📊 PERCENT DISCOUNT
        // =========================
        "PERCENT" -> {
            discountPercent.value = handleNumberInput(
                current = discountPercent.value,
                label = label,
                maxValue = 100.0
            )

            billViewModel.setPercentDiscount(
                discountPercent.value.toDoubleOrNull() ?: 0.0
            )
        }

        // =========================
        // 💳 CREDIT
        // =========================
        "CREDIT" -> {
            creditAmount.value = handleNumberInput(creditAmount.value, label)
        }

        // =========================
        // 🚚 DELIVERY
        // =========================
        "DELIVERY" -> {
            deliveryFee.value = handleNumberInput(deliveryFee.value, label)

            billViewModel.setDeliveryFee(
                deliveryFee.value.toDoubleOrNull() ?: 0.0
            )
        }
        // =========================
        // RECEIVED AMOUNT
        // =========================
        "RECEIVED_AMOUNT" -> {
            setReceivedAmount(
                handleNumberInput(
                    current = receivedAmount,
                    label = label
                )
            )
        }


        "REDEEM_POINTS" -> {
            when (label) {
                "←" -> {
                    setRedeemPointsInput(redeemPointsInput.dropLast(1))
                }

                "." -> {
                    // Ignore decimal input for whole-number points.
                }

                else -> {
                    setRedeemPointsInput(redeemPointsInput + label)
                }
            }
        }
    }
}



fun handleNumberInput(
    current: String,
    label: String,
    allowDecimal: Boolean = true,
    maxDecimals: Int = 2,
    maxValue: Double? = null
): String {

    var value = current

    when (label) {

        "←" -> {
            if (value.isNotEmpty()) {
                value = value.dropLast(1)
            }
        }

        "." -> {
            if (!allowDecimal) return value

            if (!value.contains(".")) {
                value = if (value.isEmpty()) "0." else value + "."
            }
        }

        else -> {
            if (!label.all { it.isDigit() }) return value

            // limit decimals
            if (value.contains(".")) {
                val parts = value.split(".")
                if (parts.size == 2 && parts[1].length >= maxDecimals) {
                    return value
                }
            }

            val newValue = value + label

            // limit max value if needed
            if (maxValue != null) {
                val num = newValue.toDoubleOrNull() ?: return value
                if (num > maxValue) return value
            }

            value = newValue
        }
    }

    return value
}