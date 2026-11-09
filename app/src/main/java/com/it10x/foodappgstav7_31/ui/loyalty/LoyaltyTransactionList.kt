
package com.it10x.foodappgstav7_31.ui.loyalty

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_31.data.pos.entities.loyalty.LoyaltyTransactionEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LoyaltyTransactionList(
    transactions: List<LoyaltyTransactionEntity>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Transaction History",
            style = MaterialTheme.typography.titleLarge
        )

        if (transactions.isEmpty()) {
            Text(
                text = "No loyalty transactions yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            transactions.forEach { transaction ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = transaction.transactionType.name
                                    .replace("_", " "),
                                style = MaterialTheme.typography.titleSmall
                            )

                            transaction.description
                                ?.takeIf { it.isNotBlank() }
                                ?.let { description ->
                                    Text(
                                        text = description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                            if (!transaction.orderNo.isNullOrBlank()) {
                                Text(
                                    text = "Order: ${transaction.orderNo}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }

                            Text(
                                text = SimpleDateFormat(
                                    "dd MMM yyyy, hh:mm a",
                                    Locale.getDefault()
                                ).format(Date(transaction.createdAt)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = if (transaction.points > 0)
                                "+${transaction.points}"
                            else
                                transaction.points.toString(),
                            color = if (transaction.points > 0)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}
