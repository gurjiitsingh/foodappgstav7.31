package com.it10x.foodappgstav7_31.ui.kitchen

import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
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
//import com.it10x.foodappgstav7_31.BuildConfig
import com.it10x.foodappgstav7_31.ui.cart.CartRow
import com.it10x.foodappgstav7_31.ui.cart.CartViewModel
import com.it10x.foodappgstav7_31.utils.formatter.MoneyFormatter
import com.it10x.foodappgstav7_31.utils.tax.TaxMode

@Composable
fun KitchenScreen(
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

    // Auto-close when empty
    LaunchedEffect(cartItems) {
        if (cartItems.isEmpty()) onKitchenEmpty()
    }

    if (cartItems.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No items in cart.", fontSize = 16.sp, color = Color(0xFFCCCCCC))
        }
        return
    }

    // ---------- Totals calculation ----------
    val subTotal = cartItems.sumOf { item ->
        val price = if (item.finalPrice > 0) item.finalPrice else item.basePrice
        price * item.quantity
    }

    val totalTax =
        if (outletInfo.taxMode == TaxMode.FORCE_INCLUSIVE) {
            0.0
        } else {
            cartItems.sumOf { item ->
                val price = if (item.finalPrice > 0) item.finalPrice else item.basePrice
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

    // ---------- Layout ----------
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // ✅ LEFT: Item list
        Column(
            modifier = Modifier
                .weight(0.7f)
                .fillMaxHeight()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(cartItems, key = { it.id }) { item ->
                    CartRow(
                        item = item,
                        cartViewModel = cartViewModel,
                        tableNo = tableNo,


                    )
                }
            }
        }

        // ✅ RIGHT: Totals and action buttons
        Column(
            modifier = Modifier
                .weight(0.3f)
                .fillMaxHeight()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Order Summary",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                SummaryRow("Subtotal", formattedSubTotal)
                SummaryRow("Tax", formattedTax)
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                SummaryRow(
                    "Grand Total",
                    formattedGrandTotal,
                    bold = true,
                    color = MaterialTheme.colorScheme.primary
                )
            }



            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 🔹 Send All to Kitchen
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    onClick = {
                        val deviceId = Settings.Secure.getString(
                            context.contentResolver,
                            Settings.Secure.ANDROID_ID
                        )


                        kitchenViewModel.cartToKotMainPOS(
                            orderType = orderType,
                            tableNo = tableNo!!,
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
                        contentDescription = "Send to Kitchen & Bill",
                        tint = Color.White
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Send All to Bill", color = Color.White)
                }


            }
        }
    }
}

fun parseModifierPrice(modifiersJson: String?): Double {
    if (modifiersJson.isNullOrEmpty()) return 0.0

    return try {
        val jsonArray = org.json.JSONArray(modifiersJson)
        var total = 0.0

        for (i in 0 until jsonArray.length()) {
            val group = jsonArray.getJSONObject(i)
            val items = group.optJSONArray("items") ?: continue

            for (j in 0 until items.length()) {
                val item = items.getJSONObject(j)
                total += item.optDouble("price", 0.0)
            }
        }

        total
    } catch (e: Exception) {
        0.0
    }
}


@Composable
fun SummaryRow(
    label: String,
    value: String,
    bold: Boolean = false,
    color: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = if (bold) 15.sp else 14.sp
        )

        Text(
            value,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            color = when {
                color != Color.Unspecified -> color
                else -> MaterialTheme.colorScheme.onSurface
            },
            fontSize = if (bold) 16.sp else 14.sp
        )
    }
}

