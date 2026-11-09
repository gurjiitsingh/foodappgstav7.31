package com.it10x.foodappgstav7_31.ui.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.it10x.foodappgstav7_31.data.pos.entities.PosUserEntity
import com.it10x.foodappgstav7_31.ui.components.PinPad
import com.it10x.foodappgstav7_31.ui.theme.PosTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration


@Composable
fun PosLoginScreen(

    users: List<PosUserEntity>,
    isLoading: Boolean,
    syncing: Boolean,
    error: String?,
    onLoginClick: (PosUserEntity, String) -> Unit,
    onSyncUsersClick: () -> Unit
) {

    var selectedUser by remember {
        mutableStateOf<PosUserEntity?>(null)
    }

    var password by remember {
        mutableStateOf("")
    }
    val configuration = LocalConfiguration.current

    val columns = when {
        configuration.screenWidthDp >= 900 -> 5   // Large tablet
        configuration.screenWidthDp >= 600 -> 4   // Small tablet
        configuration.screenWidthDp >= 400 -> 3   // Large phone
        else -> 2                                // Small phone
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PosTheme.topBar.background
    )
    {

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
            ) {


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "POS LOGIN",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = PosTheme.topBar.content
                )

                IconButton(
                    onClick = onSyncUsersClick,
                    enabled = !syncing
                ) {
                    if (syncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = PosTheme.accent.primaryActionBg
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Update Users",
                            tint = PosTheme.accent.primaryActionBg
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))


//            Text(
//                text = "Select User",
//                fontWeight = FontWeight.Bold
//            )


            Spacer(
                modifier = Modifier.height(6.dp)
            )


            // =========================
            // USER GRID
            // =========================

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),   // <-- occupies available space and becomes scrollable
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PosTheme.product.productCardBg
                )
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    contentPadding = PaddingValues(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                )
                {
                    items(users) { user ->


                        val selected =
                            selectedUser?.userId == user.userId



                        Card(

                            modifier = Modifier
                                .height(45.dp)
                                .fillMaxWidth()
                                .clickable {

                                    selectedUser = user
                                    password = ""

                                },


                            border =
                                if (selected)

                                    BorderStroke(
                                        2.dp,
                                        MaterialTheme.colorScheme.primary
                                    )
                                else

                                    BorderStroke(
                                        0.5.dp,
                                        MaterialTheme.colorScheme.outline
                                            .copy(alpha = 0.35f)
                                    ),


                            colors = CardDefaults.cardColors(

                                containerColor =
                                    if (selected)

                                        PosTheme.accent.primaryActionBg.copy(alpha = 0.25f)
                                    else

                                        PosTheme.product.productCardBg

                            ),


                            shape = RoundedCornerShape(8.dp)

                        ) {


                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp),

                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp),
                                    tint = PosTheme.accent.primaryActionBg
                                )


                                Spacer(
                                    modifier = Modifier.width(6.dp)
                                )




                                Text(
                                    text = user.fullName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PosTheme.product.productCardText,
                                    maxLines = 1
                                )

                                Spacer(
                                    modifier = Modifier.width(20.dp)
                                )

                                Text(
                                    text = user.role,
                                    fontSize = 11.sp,
                                    color = PosTheme.accent.primaryActionBg,
                                    maxLines = 1
                                )


                            }


                        }


                    }


                }






                Spacer(
                    modifier = Modifier.height(15.dp)
                )


                // =========================
                // PIN + KEYBOARD
                // =========================


//            Row(
//
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f),
//
//                horizontalArrangement =
//                    Arrangement.spacedBy(20.dp)
//
//            ) {


                // LEFT
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 420.dp)
                            .padding(horizontal = 16.dp)   // ← Left & right padding
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {


                    OutlinedTextField(

                        value = password,

                        onValueChange = {},

                        readOnly = true,

                        enabled = selectedUser != null,

                        modifier = Modifier.fillMaxWidth(),

                        label = {
                            Text(
                                "PIN",
                                color = PosTheme.topBar.content
                            )
                        },

                        visualTransformation =
                            PasswordVisualTransformation(),

                        colors = OutlinedTextFieldDefaults.colors(

                            focusedBorderColor =
                                PosTheme.accent.primaryActionBg,

                            unfocusedBorderColor =
                                PosTheme.topBar.content.copy(alpha = 0.5f),

                            disabledBorderColor =
                                PosTheme.topBar.content.copy(alpha = 0.2f),

                            focusedTextColor =
                                PosTheme.topBar.content,

                            unfocusedTextColor =
                                PosTheme.topBar.content,

                            disabledTextColor =
                                PosTheme.topBar.content.copy(alpha = 0.5f)

                        )

                    )



                    Spacer(
                        modifier = Modifier.height(25.dp)
                    )



                    Button(

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),

                        shape = RoundedCornerShape(4.dp),   // small round corner

                        colors = ButtonDefaults.buttonColors(

                            containerColor =
                                PosTheme.accent.primaryActionBg,

                            contentColor =
                                PosTheme.accent.primaryActionText

                        ),

                        enabled =
                            selectedUser != null &&
                                    password.isNotBlank() &&
                                    !isLoading,


                        onClick = {

                            selectedUser?.let { user ->

                                onLoginClick(
                                    user,
                                    password
                                )

                            }

                        }

                    ) {

                        Text(
                            text = "LOGIN",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                    }



                    if (error != null) {


                        Spacer(
                            modifier = Modifier.height(10.dp)
                        )


                        Text(

                            text = error,

                            color =
                                MaterialTheme.colorScheme.error

                        )


                    }
                    Spacer(
                        modifier = Modifier.height(30.dp)
                    )
                    PinPad(

                        onInput = { key ->


                            when (key) {


                                "BACK" -> {

                                    if (password.isNotEmpty()) {

                                        password =
                                            password.dropLast(1)

                                    }

                                }


                                else -> {


                                    if (password.length < 6) {

                                        password += key

                                    }

                                }


                            }


                        },


                        onClear = {

                            password = ""

                        }

                    )


                }
            }

                // RIGHT KEYBOARD




            }


        }
    }
}}}