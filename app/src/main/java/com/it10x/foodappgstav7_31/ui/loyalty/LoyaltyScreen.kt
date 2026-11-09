
package com.it10x.foodappgstav7_31.ui.loyalty

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_31.data.pos.viewmodel.loyalty.LoyaltyViewModel

@Composable
fun LoyaltyScreen(
    customerId: String,
    ownerId: String,
    outletId: String,
    viewModel: LoyaltyViewModel,
    modifier: Modifier = Modifier
) {
    val accounts by viewModel.accounts.collectAsState()
    val selectedAccount by viewModel.selectedAccount.collectAsState()
    val programs by viewModel.programs.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val rewards by viewModel.rewards.collectAsState()
    val vouchers by viewModel.vouchers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var showAddPointsDialog by remember { mutableStateOf(false) }
    var pointsInput by remember { mutableStateOf("") }
    var pointsReason by remember { mutableStateOf("") }
    var pointsInputError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(customerId, ownerId, outletId) {
        viewModel.initializeLoyalty(
            customerId = customerId,
            ownerId = ownerId,
            outletId = outletId
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // HEADER
        Text(
            text = "Customer Loyalty",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Manage customer loyalty accounts, points and rewards.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // LOADING
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }

        // ERROR
        errorMessage?.let { message ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )

                    TextButton(
                        onClick = { viewModel.clearMessages() }
                    ) {
                        Text("Dismiss")
                    }
                }
            }
        }

        // SUCCESS
        successMessage?.let { message ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    TextButton(
                        onClick = { viewModel.clearMessages() }
                    ) {
                        Text("Dismiss")
                    }
                }
            }
        }

        // LOYALTY PROGRAMMES
        Text(
            text = "Loyalty Programmes",
            style = MaterialTheme.typography.titleLarge
        )

        if (programs.isEmpty()) {
            Text(
                text = "No loyalty programmes found.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            programs.forEach { program ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = program.programName,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = program.scope.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            program.description
                                ?.takeIf { it.isNotBlank() }
                                ?.let { description ->
                                    Text(
                                        text = description,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                        }

                        Button(
                            onClick = {
                                viewModel.createOrGetAccount(
                                    customerId = customerId,
                                    programId = program.id,
                                    outletId = outletId
                                )
                            },
                            enabled = !isLoading
                        ) {
                            Text("Open Account")
                        }
                    }
                }
            }
        }

        HorizontalDivider()

        // CUSTOMER ACCOUNTS
        Text(
            text = "Customer Accounts",
            style = MaterialTheme.typography.titleLarge
        )

        if (accounts.isEmpty()) {
            Text(
                text = "This customer has no loyalty accounts yet.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            accounts.forEach { account ->
                LoyaltyAccountCard(
                    account = account,
                    isSelected = selectedAccount?.id == account.id,
                    onClick = {
                        val program = programs.firstOrNull {
                            it.id == account.programId
                        }

                        if (program != null) {
                            viewModel.selectAccount(
                                account = account,
                                programId = program.id
                            )
                        }
                    }
                )
            }
        }

        // SELECTED ACCOUNT DETAILS
        selectedAccount?.let { account ->

            HorizontalDivider()

            Text(
                text = "Selected Account",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Available balance: ${account.balancePoints} points",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // ADD POINTS BUTTON
            Button(
                onClick = {
                    pointsInput = ""
                    pointsReason = ""
                    pointsInputError = null
                    showAddPointsDialog = true
                },
                enabled = !isLoading && account.isActive,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Points")
            }

            LoyaltyTransactionList(
                transactions = transactions
            )

            HorizontalDivider()

            LoyaltyRewardList(
                rewards = rewards,
                vouchers = vouchers
            )
        }
    }

    // ADD POINTS DIALOG
    if (showAddPointsDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isLoading) {
                    showAddPointsDialog = false
                }
            },
            title = {
                Text("Add Loyalty Points")
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Enter the number of points to add to this customer's account."
                    )

                    OutlinedTextField(
                        value = pointsInput,
                        onValueChange = {
                            pointsInput = it.filter { character ->
                                character.isDigit()
                            }
                            pointsInputError = null
                        },
                        label = {
                            Text("Points")
                        },
                        placeholder = {
                            Text("Example: 100")
                        },
                        singleLine = true,
                        isError = pointsInputError != null,
                        supportingText = {
                            pointsInputError?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = pointsReason,
                        onValueChange = {
                            pointsReason = it
                        },
                        label = {
                            Text("Reason (optional)")
                        },
                        placeholder = {
                            Text("Example: Birthday bonus")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = !isLoading,
                    onClick = {
                        val points = pointsInput.toIntOrNull()

                        if (points == null || points <= 0) {
                            pointsInputError =
                                "Enter a valid number greater than zero."
                        } else {
                            viewModel.addManualPoints(
                                accountId = selectedAccount?.id.orEmpty(),
                                points = points,
                                outletId = outletId,
                                description = pointsReason
                                    .trim()
                                    .ifBlank { "Manual points added" }
                            )

                            showAddPointsDialog = false
                        }
                    }
                ) {
                    Text("Add Points")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isLoading,
                    onClick = {
                        showAddPointsDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
