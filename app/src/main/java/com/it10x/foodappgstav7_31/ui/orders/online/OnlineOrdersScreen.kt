package com.it10x.foodappgstav7_31.ui.orders.online

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.it10x.foodappgstav7_31.data.online.models.OrderMasterData
import com.it10x.foodappgstav7_31.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_31.data.pos.entities.config.OutletEntity
import com.it10x.foodappgstav7_31.printer.PrinterManager
import com.it10x.foodappgstav7_31.viewmodel.OnlineOrdersViewModel
import com.it10x.foodappgstav7_31.viewmodel.RealtimeOrdersViewModel

@Composable
fun OnlineOrdersScreen(
    printerManager: PrinterManager,
    ordersViewModel: OnlineOrdersViewModel,
    realtimeOrdersViewModel: RealtimeOrdersViewModel,
    outlet: OutletEntity?
) {
    var selectedOrder by remember {
        mutableStateOf<OrderMasterData?>(null)
    }

    val context = LocalContext.current

    // =====================================================
    // CURRENCY
    // =====================================================

    val currencyCode = outlet?.currencyCode ?: "INR"
    val localeTag = outlet?.localeTag ?: "en-IN"

    // =====================================================
    // OBSERVE LOCAL ROOM ONLINE ORDERS
    // Orders come from online_received_orders.
    // POS order history is not used for this list.
    // =====================================================

    LaunchedEffect(ordersViewModel, context) {
        val onlineOrderDao =
            AppDatabaseProvider
                .get(context.applicationContext)
                .onlineReceivedOrderDao()

        ordersViewModel.observeLocalOnlineOrders(onlineOrderDao)

        // Keep the realtime listener running for incoming-order
        // notifications, ringtone, and other existing behavior.
        realtimeOrdersViewModel.startListening()
    }

    val localOrders by
    ordersViewModel.localOnlineOrders.collectAsState()

    // =====================================================
    // ORDER DETAIL
    // =====================================================

    if (selectedOrder != null) {
        OnlineOrderDetailScreen(
            order = selectedOrder!!,
            ordersViewModel = ordersViewModel,
            realtimeOrdersViewModel = realtimeOrdersViewModel,
            currencyCode = currencyCode,
            localeTag = localeTag,
            onBack = {
                selectedOrder = null
            }
        )

        return
    }

    // =====================================================
    // SORT ROOM ORDERS: NEWEST -> OLDEST
    // =====================================================

    val onlineOrders = remember(localOrders) {
        localOrders.sortedByDescending { order ->
            order.createdAtMillis
        }
    }

    // =====================================================
    // LIST STATE
    // =====================================================

    val listState = rememberLazyListState()

    val newestOrderId = onlineOrders.firstOrNull()?.id

    // Scroll to the top when a new local online order arrives.
    LaunchedEffect(newestOrderId) {
        if (newestOrderId != null && onlineOrders.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    // =====================================================
    // SCREEN
    // =====================================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text(
            text = "Online Orders",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        when {
            onlineOrders.isEmpty() -> {
                Text(
                    text = "No online orders found",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            else -> {
                OnlineOrderTableHeader()

                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f)
                ) {
                    items(
                        items = onlineOrders,
                        key = { order -> order.id }
                    ) { order ->
                        OnlineOrderTableRow(
                            order = order,
                            currencyCode = currencyCode,
                            localeTag = localeTag,
                            onOrderClick = {
                                selectedOrder = order
                            },
                            onPrintClick = {
                                ordersViewModel.printOrder(order)
                            },
                            orderStatus = order.orderStatus ?: "NEW",

                            onStatusChange = { newStatus ->

                                val onlineOrderDao = AppDatabaseProvider
                                    .get(context.applicationContext)
                                    .onlineReceivedOrderDao()

                                realtimeOrdersViewModel.updateOrderStatus(
                                    orderId = order.id,
                                    status = newStatus
                                ) { success, error ->

                                    if (success) {
                                        ordersViewModel.updateLocalOrderStatus(
                                            dao = onlineOrderDao,
                                            orderId = order.id,
                                            status = newStatus
                                        )
                                    } else {
                                        android.util.Log.e(
                                            "ORDER_STATUS",
                                            error ?: "Failed to update Firestore"
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
