package com.it10x.foodappgstav7_31.ui.orders.online

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_31.data.online.models.OrderMasterData
import com.it10x.foodappgstav7_31.data.online.models.OrderProductData
import com.it10x.foodappgstav7_31.data.online.models.fullDeliveryAddress
import com.it10x.foodappgstav7_31.utils.formatter.MoneyFormatter
import com.it10x.foodappgstav7_31.viewmodel.OnlineOrdersViewModel
import com.it10x.foodappgstav7_31.viewmodel.RealtimeOrdersViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineOrderDetailScreen(
    order: OrderMasterData,
    ordersViewModel: OnlineOrdersViewModel,
    realtimeOrdersViewModel: RealtimeOrdersViewModel,
    currencyCode: String,
    localeTag: String,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var orderItems by remember {
        mutableStateOf<List<OrderProductData>>(emptyList())
    }

    var loading by remember {
        mutableStateOf(true)
    }

    // =====================================================
    // LOAD ORDER ITEMS
    // =====================================================

    LaunchedEffect(order.id) {
        loading = true

        orderItems = ordersViewModel.getOrderItems(order.id)

        loading = false
    }

    // =====================================================
    // SCREEN
    // =====================================================

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Order #${order.srno}",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                },

                navigationIcon = {
                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp)
        ) {

            // =================================================
            // ORDER SUMMARY CARD
            // =================================================

            Card(
                modifier = Modifier.fillMaxWidth(),

                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),

                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),

                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {

                    // =========================================
                    // LEFT - AMOUNTS
                    // =========================================

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = "Amount",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(
                            Modifier.height(6.dp)
                        )

                        Text(
                            text = "Item Total: ${
                                MoneyFormatter.format(
                                    amount = anyToDouble(order.itemTotal),
                                    currencyCode = currencyCode,
                                    localeTag = localeTag
                                )
                            }",
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        order.discountTotal?.let {

                            Text(
                                text = "Discount: ${
                                    MoneyFormatter.format(
                                        amount = anyToDouble(it),
                                        currencyCode = currencyCode,
                                        localeTag = localeTag
                                    )
                                }",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        order.subTotal?.let {

                            Text(
                                text = "Subtotal: ${
                                    MoneyFormatter.format(
                                        amount = anyToDouble(it),
                                        currencyCode = currencyCode,
                                        localeTag = localeTag
                                    )
                                }",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        order.taxTotal?.let {

                            Text(
                                text = "Tax: ${
                                    MoneyFormatter.format(
                                        amount = anyToDouble(it),
                                        currencyCode = currencyCode,
                                        localeTag = localeTag
                                    )
                                }",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        order.deliveryFee?.let {

                            Text(
                                text = "Delivery Fee: ${
                                    MoneyFormatter.format(
                                        amount = anyToDouble(it),
                                        currencyCode = currencyCode,
                                        localeTag = localeTag
                                    )
                                }",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(
                            Modifier.height(8.dp)
                        )

                        order.grandTotal?.let {

                            Text(
                                text = "Grand Total: ${
                                    MoneyFormatter.format(
                                        amount = anyToDouble(it),
                                        currencyCode = currencyCode,
                                        localeTag = localeTag
                                    )
                                }",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // =========================================
                    // RIGHT - CUSTOMER + ORDER
                    // =========================================

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = "Customer",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(
                            Modifier.height(6.dp)
                        )

                        Text(
                            text = order.customerName.ifBlank {
                                "Walk-in"
                            },
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )

                        order.customerPhone?.let {

                            Text(
                                text = "📞 $it",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        order.email
                            .takeIf { it.isNotBlank() }
                            ?.let {

                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                        order.fullDeliveryAddress()?.let {

                            Spacer(
                                Modifier.height(4.dp)
                            )

                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(
                            Modifier.height(10.dp)
                        )

                        Text(
                            text = "Order",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(
                            Modifier.height(4.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            Text(
                                text = "Source: ${order.source ?: "POS"}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = "Type: ${order.orderType ?: "—"}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        order.tableNo?.let {

                            Text(
                                text = "Table: $it",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            Text(
                                text = "Status: ${order.orderStatus ?: "NEW"}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = "Payment: ${order.paymentType} " +
                                        "(${order.paymentStatus ?: "PENDING"})",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(
                Modifier.height(12.dp)
            )

            // =================================================
            // ORDER ITEMS
            // =================================================

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.background
                    )
            ) {

                if (loading) {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }

                } else if (orderItems.isEmpty()) {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = "No items found",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                } else
                {

                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {

                        items(
                            items = orderItems
                        ) { item ->

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        vertical = 10.dp,
                                        horizontal = 4.dp
                                    )
                            ) {

                                // =================================
                                // ITEM NAME + SUBTOTAL
                                // =================================

                                Row(
                                    modifier = Modifier.fillMaxWidth(),

                                    horizontalArrangement =
                                        Arrangement.SpaceBetween,

                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {

                                    Text(
                                        text = item.name,

                                        style =
                                            MaterialTheme.typography.bodyLarge,

                                        fontWeight =
                                            FontWeight.SemiBold,

                                        color =
                                            MaterialTheme.colorScheme.onBackground
                                    )

                                    Text(
                                        text =
                                            MoneyFormatter.format(
                                                amount = anyToDouble(
                                                    item.itemSubtotal
                                                ),
                                                currencyCode = currencyCode,
                                                localeTag = localeTag
                                            ),

                                        style =
                                            MaterialTheme.typography.bodyLarge,

                                        fontWeight =
                                            FontWeight.Medium,

                                        color =
                                            MaterialTheme.colorScheme.onBackground,

                                        fontFamily =
                                            FontFamily.Monospace
                                    )
                                }

                                Spacer(
                                    Modifier.height(3.dp)
                                )

                                // =================================
                                // QTY × PRICE
                                // =================================

                                Text(
                                    text =
                                        "${item.quantity} × ${
                                            MoneyFormatter.format(
                                                amount = anyToDouble(item.basePrice),
                                                currencyCode = currencyCode,
                                                localeTag = localeTag
                                            )
                                        }",

                                    style =
                                        MaterialTheme.typography.bodySmall,

                                    color =
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // =================================
                                // PRICE PER ITEM
                                // =================================

                                Row(
                                    modifier = Modifier.fillMaxWidth(),

                                    horizontalArrangement =
                                        Arrangement.End
                                ) {

                                    Text(
                                        text =
                                            "${
                                                MoneyFormatter.format(
                                                    amount = anyToDouble(
                                                        item.basePrice
                                                    ),
                                                    currencyCode = currencyCode,
                                                    localeTag = localeTag
                                                )
                                            } / item",

                                        style =
                                            MaterialTheme.typography.labelSmall,

                                        color =
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                HorizontalDivider(
                                    modifier =
                                        Modifier.padding(top = 8.dp),

                                    thickness = 0.6.dp,

                                    color =
                                        MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(
                Modifier.height(12.dp)
            )

            // =================================================
            // ACTION BUTTONS
            // =================================================

            Row(
                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                Button(
                    modifier = Modifier.weight(1f),

                    onClick = {
                        ordersViewModel.printOrder(order)
                    }
                ) {
                    Text("Print")
                }

                Button(
                    modifier = Modifier.weight(1f),

                    onClick = {
                        scope.launch {
                            realtimeOrdersViewModel
                                .acknowledgeOrder(order.id)
                        }
                    },

                    enabled = order.acknowledged != true
                ) {

                    Text(
                        if (order.acknowledged == true)
                            "Acknowledged"
                        else
                            "Acknowledge"
                    )
                }
            }
        }
    }
}

// =====================================================
// NUMBER HELPER
// =====================================================

fun anyToDouble(value: Any?): Double {
    return when (value) {
        is Number -> value.toDouble()

        is String -> {
            value
                .replace(",", "")
                .trim()
                .toDoubleOrNull() ?: 0.0
        }

        else -> {
            value?.toString()
                ?.replace(",", "")
                ?.trim()
                ?.toDoubleOrNull()
                ?: 0.0
        }
    }
}