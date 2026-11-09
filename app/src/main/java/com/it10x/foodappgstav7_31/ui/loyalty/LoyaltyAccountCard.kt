
package com.it10x.foodappgstav7_31.ui.loyalty

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_31.data.pos.entities.loyalty.LoyaltyAccountEntity

@Composable
fun LoyaltyAccountCard(
    account: LoyaltyAccountEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary
            )
        } else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (account.scopeKey == "SHARED")
                        "Shared Loyalty Account"
                    else
                        "Outlet Loyalty Account",
                    style = MaterialTheme.typography.titleMedium
                )

                if (isSelected) {
                    Text(
                        text = "SELECTED",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Text(
                text = "${account.balancePoints} Points",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Lifetime earned",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = account.lifetimeEarned.toString(),
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                Column {
                    Text(
                        text = "Lifetime redeemed",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = account.lifetimeRedeemed.toString(),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            if (account.outletId != null) {
                Text(
                    text = "Outlet: ${account.outletId}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
