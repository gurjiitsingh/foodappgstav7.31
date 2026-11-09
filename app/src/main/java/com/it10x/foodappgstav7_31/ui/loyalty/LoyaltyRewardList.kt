package com.it10x.foodappgstav7_31.ui.loyalty

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_31.data.pos.entities.loyalty.LoyaltyRewardEntity
import com.it10x.foodappgstav7_31.data.pos.entities.loyalty.LoyaltyVoucherEntity

@Composable
fun LoyaltyRewardList(
    rewards: List<LoyaltyRewardEntity>,
    vouchers: List<LoyaltyVoucherEntity>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // REWARDS
        Text(
            text = "Available Rewards",
            style = MaterialTheme.typography.titleMedium
        )

        if (rewards.isEmpty()) {
            Text(
                text = "No rewards available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            rewards.forEach { reward ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = reward.rewardName,
                            style = MaterialTheme.typography.titleSmall
                        )

                        reward.description
                            ?.takeIf { it.isNotBlank() }
                            ?.let { description ->
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                        Text(
                            text = "${reward.pointsRequired} points required",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = reward.rewardType.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // VOUCHERS
        Text(
            text = "Active Vouchers",
            style = MaterialTheme.typography.titleMedium
        )

        if (vouchers.isEmpty()) {
            Text(
                text = "No active vouchers",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            vouchers.forEach { voucher ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = voucher.voucherCode,
                            style = MaterialTheme.typography.titleSmall
                        )

                        Text(
                            text = voucher.voucherType.name,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "Used ${voucher.usedCount} / ${voucher.usageLimit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}