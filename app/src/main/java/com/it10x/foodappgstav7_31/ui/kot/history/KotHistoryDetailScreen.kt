package com.it10x.foodappgstav7_31.ui.kot.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.it10x.foodappgstav7_31.data.pos.entities.PosKotHistoryEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KotHistoryDetailScreen(
    batchId: String,
    viewModel: KotHistoryViewModel,
    navController: NavController
) {

    val history by viewModel.history.collectAsState()

    val items = history.filter { it.batchId == batchId }

    if (items.isEmpty()) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Text("No KOT Found")

        }

        return
    }

    val header = items.first()

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("KOT ${header.kotNumber}")
                },

                navigationIcon = {

                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {

                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )

                    }

                },

                colors = TopAppBarDefaults.topAppBarColors()

            )

        }

    ) { padding ->

        LazyColumn(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),

            verticalArrangement = Arrangement.spacedBy(10.dp)

        ) {

            item {

                HeaderCard(header)

            }

            items(items) { item ->

                ItemCard(item)

            }

        }

    }

}

@Composable
private fun HeaderCard(
    item: PosKotHistoryEntity
) {

    val statusColor = when (item.status) {

        "ACTIVE" -> Color(0xFF2E7D32)

        "PAID" -> Color(0xFF1565C0)

        "DELETED" -> Color.Red

        else -> Color.Gray

    }

    Card(
        elevation = CardDefaults.cardElevation(4.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = item.kotNumber,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Divider()

            Spacer(modifier = Modifier.height(10.dp))

            DetailRow(
                "Table",
                item.tableName
            )

            DetailRow(
                "Order Type",
                item.orderType
            )

            DetailRow(
                "Source",
                item.source
            )

            DetailRow(
                "Status",
                item.status,
                statusColor
            )

            DetailRow(
                "Created",
                formatDate(item.createdAt)
            )

            item.paidAt?.let {

                DetailRow(
                    "Paid",
                    formatDate(it)
                )

            }

        }

    }

}

@Composable
private fun ItemCard(
    item: PosKotHistoryEntity
) {

    Card(
        elevation = CardDefaults.cardElevation(2.dp)
    ) {

        Column(
            modifier = Modifier.padding(14.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "x${item.quantity}",
                    fontWeight = FontWeight.Bold
                )

            }

            if (!item.note.isNullOrBlank()) {

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Note: ${item.note}",
                    color = Color.Gray
                )

            }

            if (item.modifierTotal > 0) {

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Modifier Total : ${item.modifierTotal}"
                )

            }

            if (item.deleted) {

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Deleted",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )

                item.deletedReason?.let {

                    Text(
                        text = "Reason : $it",
                        color = Color.Red
                    )

                }

            }

        }

    }

}

@Composable
private fun DetailRow(
    title: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),

        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = title,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = value,
            color = valueColor
        )

    }

}

private fun formatDate(
    millis: Long
): String {

    return SimpleDateFormat(
        "dd MMM yyyy  hh:mm a",
        Locale.getDefault()
    ).format(Date(millis))

}