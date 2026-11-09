package com.it10x.foodappgstav7_31.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun PinPad(
    onInput: (String) -> Unit,
    onClear: () -> Unit
) {

    val buttons = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("C", "0", "←")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {

        buttons.forEach { row ->

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {

                row.forEach { label ->

                    Button(
                        onClick = {

                            when(label) {
                                "C" -> onClear()
                                "←" -> onInput("BACK")
                                else -> onInput(label)
                            }

                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(45.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(2.dp)
                    ) {

                        Text(
                            text = label,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                    }
                }
            }
        }
    }
}