package com.it10x.foodappgstav7_31.ui.dayclosing

import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_31.ui.theme.PosTheme
import com.it10x.foodappgstav7_31.utils.formatter.MoneyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import java.time.Instant
import java.time.ZoneId
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayClosingScreen(
    viewModel: DayClosingViewModel,
    currencyCode: String,
    localeTag: String,
    onCloseDay: () -> Unit
) {

    val ui by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isPhone = configuration.screenWidthDp < 600
    LaunchedEffect(ui.errorMessage) {
        ui.errorMessage?.let { message ->

            Toast.makeText(
                context,
                "Day already closed",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PosTheme.bill.billBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    )
    {

        Text(
            text = "Business Day Closing",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = PosTheme.bill.billText
        )



        //==========================================================
        // ROW 1
        //==========================================================


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        )
        {

            Text(
                text = "Selected Date: ${viewModel.selectedDate}",
                color = PosTheme.bill.billText
            )

            OutlinedButton(onClick = { showDatePicker = true }) {
                Text("Select Date")
            }
        }
        //==========================================================
        // ROW 2
        //==========================================================
        if (showDatePicker) {

            val datePickerState = rememberDatePickerState()

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val millis = datePickerState.selectedDateMillis
                            if (millis != null) {
                                val date = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()

                                viewModel.updateSelectedDate(date)
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }


        if (isPhone) {

            // ================= PHONE UI =================

            // 1. Cash Count - full width
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = PosTheme.bill.billTab
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Cash Count",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PosTheme.bill.billText
                    )

                    // Opening and Expected
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SmallMoneyCard(
                            modifier = Modifier.weight(1f),
                            title = "Opening",
                            amount = ui.openingCash,
                            currencyCode = currencyCode,
                            localeTag = localeTag
                        )

                        SmallMoneyCard(
                            modifier = Modifier.weight(1f),
                            title = "Expected",
                            amount = ui.expectedCash,
                            currencyCode = currencyCode,
                            localeTag = localeTag
                        )
                    }

                    OutlinedTextField(
                        value = ui.actualCash,
                        onValueChange = viewModel::updateActualCash,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Actual Cash Counted") }
                    )

                    OutlinedTextField(
                        value = ui.cashHandedOver,
                        onValueChange = viewModel::updateCashHandedOver,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Cash Handed Over") }
                    )

                    OutlinedTextField(
                        value = ui.notes,
                        onValueChange = viewModel::updateNotes,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        label = { Text("Notes (Optional)") }
                    )

                    Button(
                        onClick = onCloseDay,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Close Day")
                    }
                }
            }

            // 2. Business Information - separate container
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = PosTheme.bill.billTab
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Business Information",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = PosTheme.bill.billText
                    )

                    HorizontalDivider(color = PosTheme.bill.inputBorder)

                    InfoRow("Business Date", ui.businessDate)
                    InfoRow("Opened By", ui.openedBy)
                    InfoRow("Opened At", formatTime(ui.openedAt))
                }
            }

            // 3. Sales Summary - separate container
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = PosTheme.bill.billTab
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Sales Summary",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = PosTheme.bill.billText
                    )

                    HorizontalDivider(color = PosTheme.bill.inputBorder)

                    MoneyRow("Total Sales", ui.totalSales, currencyCode, localeTag)
                    MoneyRow("Discount", ui.totalDiscount, currencyCode, localeTag)
                    MoneyRow("Tax", ui.totalTax, currencyCode, localeTag)
                    MoneyRow("Refund", ui.totalRefund, currencyCode, localeTag)
                    MoneyRow(
                        "Complimentary",
                        ui.complimentarySales,
                        currencyCode,
                        localeTag
                    )

                    HorizontalDivider(color = PosTheme.bill.inputBorder)

                    InfoRow("Orders", ui.totalOrders.toString())
                }
            }

            // 4. Payment Breakdown - one full-width container
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = PosTheme.bill.billTab
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Payment Breakdown",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = PosTheme.bill.billText
                    )

                    HorizontalDivider(color = PosTheme.bill.inputBorder)

                    PaymentRow(
                        title = "Cash",
                        amount = ui.cashSales,
                        currencyCode = currencyCode,
                        localeTag = localeTag
                    )
                    PaymentRow(
                        title = "Card",
                        amount = ui.cardSales,
                        currencyCode = currencyCode,
                        localeTag = localeTag
                    )
                    PaymentRow(
                        title = "UPI",
                        amount = ui.upiSales,
                        currencyCode = currencyCode,
                        localeTag = localeTag
                    )
                    PaymentRow(
                        title = "Wallet",
                        amount = ui.walletSales,
                        currencyCode = currencyCode,
                        localeTag = localeTag
                    )
                    PaymentRow(
                        title = "Credit",
                        amount = ui.creditSales,
                        currencyCode = currencyCode,
                        localeTag = localeTag
                    )
                }
            }

        } else {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            )
            {

                //==========================================================
                // LEFT - Payment Breakdown
                //==========================================================



                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = PosTheme.bill.billTab
                    )
                )
                {

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    )
                    {

                        Text(
                            text = "Cash Count",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PosTheme.bill.billText
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            SmallMoneyCard(
                                modifier = Modifier.weight(1f),
                                title = "Opening",
                                amount = ui.openingCash,
                                currencyCode = currencyCode,
                                localeTag = localeTag
                            )

                            SmallMoneyCard(
                                modifier = Modifier.weight(1f),
                                title = "Expected",
                                amount = ui.expectedCash,
                                currencyCode = currencyCode,
                                localeTag = localeTag
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        )
                        {

                            OutlinedTextField(
                                value = ui.actualCash,
                                onValueChange = viewModel::updateActualCash,
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                label = {
                                    Text("Actual Cash Counted")
                                }
                            )

                            OutlinedTextField(
                                value = ui.cashHandedOver,
                                onValueChange = viewModel::updateCashHandedOver,
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                label = {
                                    Text("Cash Handed Over")
                                }
                            )

                            Button(
                                modifier = Modifier.height(56.dp),
                                onClick = onCloseDay
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null
                                )

                                Spacer(Modifier.width(8.dp))

                                Text("Close Day")
                            }
                        }

                        OutlinedTextField(
                            value = ui.notes,
                            onValueChange = viewModel::updateNotes,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp),
                            label = { Text("Notes (Optional)") }
                        )




                    }







                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    //==========================================================
                    // Business Information
                    //==========================================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = PosTheme.bill.billTab
                        )
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            Text(
                                "Business Information",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = PosTheme.bill.billText
                            )

                            HorizontalDivider(
                                color = PosTheme.bill.inputBorder
                            )

                            InfoRow("Business Date", ui.businessDate)
                            InfoRow("Opened By", ui.openedBy)
                            InfoRow("Opened At", formatTime(ui.openedAt))
                        }
                    }

                    //==========================================================
                    // Sales Summary
                    //==========================================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = PosTheme.bill.billTab
                        )
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            Text(
                                "Sales Summary",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = PosTheme.bill.billText
                            )

                            HorizontalDivider(
                                color = PosTheme.bill.inputBorder
                            )

                            MoneyRow("Total Sales", ui.totalSales, currencyCode, localeTag)
                            MoneyRow("Discount", ui.totalDiscount, currencyCode, localeTag)
                            MoneyRow("Tax", ui.totalTax, currencyCode, localeTag)
                            MoneyRow("Refund", ui.totalRefund, currencyCode, localeTag)
                            MoneyRow("Complimentary", ui.complimentarySales, currencyCode, localeTag)

                            HorizontalDivider(
                                color = PosTheme.bill.inputBorder
                            )

                            InfoRow("Orders", ui.totalOrders.toString())
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            )
            {




                //SECNOD CARD
            }

            //==========================================================
            // ROW 2 - PAYMENT
            //==========================================================

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = PosTheme.bill.billTab
                )
            )
            {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        "Payment Breakdown",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = PosTheme.bill.billText
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        PaymentCard(
                            "Cash",
                            ui.cashSales,
                            currencyCode,
                            localeTag,
                            Modifier.weight(1f)
                        )

                        PaymentCard(
                            "Card",
                            ui.cardSales,
                            currencyCode,
                            localeTag,
                            Modifier.weight(1f)
                        )

                        PaymentCard(
                            "UPI",
                            ui.upiSales,
                            currencyCode,
                            localeTag,
                            Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        PaymentCard(
                            "Wallet",
                            ui.walletSales,
                            currencyCode,
                            localeTag,
                            Modifier.weight(1f)
                        )

                        PaymentCard(
                            "Credit",
                            ui.creditSales,
                            currencyCode,
                            localeTag,
                            Modifier.weight(1f)
                        )

                        Spacer(
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

        }




    }
}


@Composable
private fun SmallMoneyCard(
    modifier: Modifier = Modifier,
    title: String,
    amount: Double,
    currencyCode: String,
    localeTag: String
) {

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = PosTheme.bill.billTab
        )
    ) {

        Column(
            modifier = Modifier.padding(12.dp)
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = PosTheme.bill.billText.copy(alpha = 0.75f)
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = MoneyFormatter.format(
                    amount,
                    currencyCode,
                    localeTag
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PosTheme.bill.billText
            )
        }
    }
}
@Composable
private fun PaymentCard(
    title: String,
    amount: Double,
    currencyCode: String,
    localeTag: String,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = PosTheme.bill.inputBg
        )
    ) {

        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = PosTheme.bill.billText.copy(alpha = 0.75f)
            )

            Text(
                text = MoneyFormatter.format(
                    amount,
                    currencyCode,
                    localeTag
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PosTheme.bill.billText
            )
        }
    }
}
@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = label,
            color = PosTheme.bill.billText.copy(alpha = 0.75f)
        )

        Text(
            text = value,
            color = PosTheme.bill.billText,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun MoneyRow(
    label: String,
    amount: Double,
    currencyCode: String,
    localeTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = label,
            color = PosTheme.bill.billText.copy(alpha = 0.75f)
        )

        Text(
            text = MoneyFormatter.format(
                amount = amount,
                currencyCode = currencyCode,
                localeTag = localeTag
            ),
            color = PosTheme.bill.billText,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatTime(time: Long): String {

    if (time == 0L) return "-"

    return SimpleDateFormat(
        "dd MMM yyyy  hh:mm a",
        Locale.getDefault()
    ).format(Date(time))
}

@Composable
private fun PaymentRow(
    title: String,
    amount: Double,
    currencyCode: String,
    localeTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = PosTheme.bill.billText.copy(alpha = 0.75f)
        )

        Text(
            text = MoneyFormatter.format(
                amount = amount,
                currencyCode = currencyCode,
                localeTag = localeTag
            ),
            fontWeight = FontWeight.SemiBold,
            color = PosTheme.bill.billText
        )
    }
}