package com.it10x.foodappgstav7_31.ui.kitchen

import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.it10x.foodappgstav7_31.data.print.OutletInfo
import com.it10x.foodappgstav7_31.ui.cart.CartRow
import com.it10x.foodappgstav7_31.ui.cart.CartRowPhone
import com.it10x.foodappgstav7_31.ui.cart.CartViewModel
import com.it10x.foodappgstav7_31.utils.formatter.MoneyFormatter
import com.it10x.foodappgstav7_31.utils.tax.TaxMode

@Composable
fun KitchenScreenPhone(
    sessionId: String,
    tableNo: String,
    tableName: String,
    orderType: String,
    kitchenViewModel: KitchenViewModel,
    cartViewModel: CartViewModel,
    outletInfo: OutletInfo,
    onKitchenEmpty: () -> Unit
) {

    val cartItems by cartViewModel.cart.collectAsState(initial = emptyList())
    val context = LocalContext.current

    // =========================================================
    // AUTO CLOSE WHEN CART IS EMPTY
    // =========================================================

    LaunchedEffect(cartItems) {
        if (cartItems.isEmpty()) {
            onKitchenEmpty()
        }
    }

    if (cartItems.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No items in cart.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        return
    }

    // =========================================================
    // TOTALS
    // =========================================================

    val subTotal = cartItems.sumOf { item ->

        val price =
            if (item.finalPrice > 0)
                item.finalPrice
            else
                item.basePrice

        price * item.quantity
    }

    val totalTax =
        if (outletInfo.taxMode == TaxMode.FORCE_INCLUSIVE) {

            0.0

        } else {

            cartItems.sumOf { item ->

                val price =
                    if (item.finalPrice > 0)
                        item.finalPrice
                    else
                        item.basePrice

                ((price * item.taxRate) / 100.0) * item.quantity
            }
        }

    val grandTotal = subTotal + totalTax

    val formattedSubTotal = MoneyFormatter.format(
        amount = subTotal,
        currencyCode = outletInfo.currencyCode,
        localeTag = outletInfo.localeTag
    )

    val formattedTax = MoneyFormatter.format(
        amount = totalTax,
        currencyCode = outletInfo.currencyCode,
        localeTag = outletInfo.localeTag
    )

    val formattedGrandTotal = MoneyFormatter.format(
        amount = grandTotal,
        currencyCode = outletInfo.currencyCode,
        localeTag = outletInfo.localeTag
    )

    // =========================================================
    // MOBILE VERTICAL LAYOUT
    // =========================================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp)
    ) {

        // =====================================================
        // ITEM LIST
        // =====================================================

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(
                top = 4.dp,
                bottom = 10.dp
            ),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            items(
                items = cartItems,
                key = { it.id }
            ) { item ->

                CartRowPhone(
                    item = item,
                    cartViewModel = cartViewModel,
                    tableNo = tableNo
                )
            }
        }

        // =====================================================
        // ORDER SUMMARY
        // =====================================================

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {

                Text(
                    text = "Order Summary",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(
                        alpha = 0.4f
                    )
                )

                SummaryRow(
                    label = "Subtotal",
                    value = formattedSubTotal
                )

                SummaryRow(
                    label = "Tax",
                    value = formattedTax
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(
                        alpha = 0.4f
                    )
                )

                SummaryRow(
                    label = "Grand Total",
                    value = formattedGrandTotal,
                    bold = true,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // =====================================================
        // SEND ALL TO BILL
        // =====================================================

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(bottom = 8.dp),

            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF16A34A),
                contentColor = Color.White
            ),

            onClick = {

                val deviceId = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ANDROID_ID
                )

                kitchenViewModel.cartToKotMainPOS(
                    orderType = orderType,
                    tableNo = tableNo,
                    tableName = tableName,
                    sessionId = sessionId,
                    paymentType = "UNPAID",
                    deviceId = deviceId,
                    deviceName = Build.MODEL ?: "Unknown Device",
                    appVersion = "BuildConfig.VERSION_NAME",
                    role = "MAINPOS"
                )
            }
        ) {

            Icon(
                imageVector = Icons.Default.SoupKitchen,
                contentDescription = "Send to Kitchen & Bill"
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Text(
                text = "Send All to Bill",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


// =============================================================
// SUMMARY ROW
// =============================================================

@Composable
fun KitchenSummaryRowPhone(
    label: String,
    value: String,
    bold: Boolean = false,
    color: Color = Color.Unspecified
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = label,
            fontWeight =
                if (bold)
                    FontWeight.SemiBold
                else
                    FontWeight.Normal,

            color = MaterialTheme.colorScheme.onSurface,

            fontSize =
                if (bold)
                    15.sp
                else
                    14.sp
        )

        Text(
            text = value,

            fontWeight =
                if (bold)
                    FontWeight.Bold
                else
                    FontWeight.Medium,

            color =
                if (color != Color.Unspecified)
                    color
                else
                    MaterialTheme.colorScheme.onSurface,

            fontSize =
                if (bold)
                    16.sp
                else
                    14.sp
        )
    }
}