package com.it10x.foodappgstav7_31.ui.pos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.it10x.foodappgstav7_31.ui.cart.CartViewModel
import com.it10x.foodappgstav7_31.utils.formatter.MoneyFormatter
@Composable
fun OrderSummaryCompact(
    cartViewModel: CartViewModel,
    currencyCode: String,
    localeTag: String,
    taxMode: String,
) {
    val cartItems by cartViewModel.cart.collectAsState(initial = emptyList())
    val itemCount = cartItems.sumOf { it.quantity }
    val itemSubtotalTotal = com.it10x.foodappgstav7_31.data.pos.viewmodel.OrderCalculator.grandTotal(cartItems,taxMode)

    val formattedItemSubtotalTotal = MoneyFormatter.format(
        amount = itemSubtotalTotal,
        currencyCode = currencyCode,
        localeTag = localeTag
    )
    Column(
        modifier = Modifier.padding(horizontal = 6.dp),
        horizontalAlignment = Alignment.Start
    ) {
        if (itemCount > 0) {
            Text(
                text = "$itemCount item${if (itemCount > 1) "s" else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = formattedItemSubtotalTotal,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )


    }
}

