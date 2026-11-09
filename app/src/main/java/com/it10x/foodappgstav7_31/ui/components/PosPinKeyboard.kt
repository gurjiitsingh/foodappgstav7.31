package com.it10x.foodappgstav7_31.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PosPinKeyboard(
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit
) {

    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("CLR", "0", "⌫")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        keys.forEach { row ->

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                row.forEach { key ->

                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .height(45.dp),
                        contentPadding = PaddingValues(0.dp),
                        onClick = {

                            when(key) {
                                "CLR" -> onClear()
                                "⌫" -> onBackspace()
                                else -> onKeyPress(key)
                            }

                        }
                    ) {

                        Text(
                            text = key,
                            style = MaterialTheme.typography.titleMedium
                        )

                    }
                }
            }
        }
    }
}