package com.it10x.foodappgstav7_31.ui.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.it10x.foodappgstav7_31.data.pos.entities.PosCartEntity
import com.it10x.foodappgstav7_31.ui.theme.PosTheme

@Composable
fun CartRowPhone(
    item: PosCartEntity,
    tableNo: String,
    cartViewModel: CartViewModel
)
{

    var showNoteDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

//        // ✏️ Edit
//        IconButton(
//            onClick = { showNoteDialog = true },
//            modifier = Modifier.size(32.dp)
//        ) {
//            Icon(
//                imageVector = Icons.Default.Edit,
//                contentDescription = "Add Note",
//                tint = MaterialTheme.colorScheme.primary
//            )
//        }

        // 🧾 NAME COLUMN
        // 🧾 ITEM DETAILS + ACTIONS
        Column(
            modifier = Modifier.weight(1f)
        ) {

            // Item name
            Text(
                text = item.name,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Modifiers
            val modifiers = ModifierJsonHelper.fromJson(item.modifiersJson)
                .flatMap { group ->
                    group.items.map { modifierItem ->
                        "${modifierItem.name} (+₹${"%.2f".format(modifierItem.price)})"
                    }
                }

            modifiers.forEach { modText ->
                Text(
                    text = "  + $modText",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Note
            item.note?.let { note ->
                if (note.isNotBlank()) {
                    Text(
                        text = "📝 $note",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 👇 ACTION BUTTONS UNDER ITEM
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // ✏️ EDIT / NOTE
                IconButton(
                    onClick = { showNoteDialog = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Add Note",
                        modifier = Modifier.size(17.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.width(45.dp))
                // 🍳 KITCHEN PRINT
                IconButton(
                    onClick = { cartViewModel.togglePrint(item) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SoupKitchen,
                        contentDescription = "Kitchen Print",
                        modifier = Modifier.size(18.dp),
                        tint = if (item.kitchenPrintReq)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                }
            }
        }


        Row(
            modifier = Modifier.width(110.dp),
            verticalAlignment = Alignment.CenterVertically,

            horizontalArrangement = Arrangement.SpaceBetween
        ) {



            IconButton(
                onClick = { cartViewModel.decrease(item.productId, tableNo) },
                modifier = Modifier
                    .size(20.dp)
                    .background(
                        PosTheme.accent.cartAddBg,
                        shape = MaterialTheme.shapes.small
                    )
            ) {
                Text(
                    "-",
                    color = PosTheme.accent.cartAddText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }


            Text(
                text = item.quantity.toString(),
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            // ➕ Add
            IconButton(
                onClick = { cartViewModel.increase(item) },
                modifier = Modifier
                    .size(20.dp)
                    .background(
                        PosTheme.accent.cartAddBg,
                        shape = MaterialTheme.shapes.small
                    )
            ) {
                Text(
                    "+",
                    color = PosTheme.accent.cartAddText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }





    }

    Spacer(Modifier.height(3.dp))
    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    Spacer(Modifier.height(3.dp))

    if (showNoteDialog) {

        var noteText by remember { mutableStateOf(item.note ?: "") }

        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("Kitchen Note") },
            text = {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Special Instructions") },
                    singleLine = false
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        cartViewModel.updateNote(item, noteText)
                        showNoteDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showNoteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }


}