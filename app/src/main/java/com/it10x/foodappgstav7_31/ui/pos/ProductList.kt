package com.it10x.foodappgstav7_31.ui.pos

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.it10x.foodappgstav7_31.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_31.data.pos.entities.PosCartEntity
import com.it10x.foodappgstav7_31.data.pos.entities.ProductEntity
import com.it10x.foodappgstav7_31.data.pos.model.ModifierGroupWithItems
import com.it10x.foodappgstav7_31.ui.cart.CartViewModel
import com.it10x.foodappgstav7_31.ui.theme.*
import com.it10x.foodappgstav7_31.viewmodel.PosTableViewModel
import com.it10x.foodappgstav7_31.data.pos.repository.ModifierRepository
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.gson.Gson
import com.it10x.foodappgstav7_31.data.pos.models.CartModifier
import com.it10x.foodappgstav7_31.data.pos.models.CartModifierItem
import com.it10x.foodappgstav7_31.utils.formatter.MoneyFormatter


@Composable
fun ProductList(
    filteredProducts: List<ProductEntity>,
   // variants: List<ProductEntity>,
    cartViewModel: CartViewModel,
    tableViewModel: PosTableViewModel,
    tableNo: String,
    posSessionViewModel: PosSessionViewModel,
    currencyCode: String,
    localeTag: String,
    onProductAdded: () -> Unit
) {

    val db = AppDatabaseProvider.get(LocalContext.current)

    val sessionId by posSessionViewModel.sessionId.collectAsState()
    val sortedProducts = remember(filteredProducts) {
        filteredProducts
            .filter { it.type == "parent" } // ✅ only parent products
            .sortedBy { it.sortOrder }
    }


    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        items(
            count = sortedProducts.size,
            key = { index -> sortedProducts[index].id }
        ) { index ->

            val product = sortedProducts[index]

            ParentProductCard(
                product = product,
                filteredProducts = filteredProducts,
                cartViewModel = cartViewModel,
                tableViewModel = tableViewModel,
                tableNo = tableNo,
                sessionId = sessionId,
                currencyCode,
                localeTag,
                onProductAdded = onProductAdded,

            )
        }

    }
}

@Composable
private fun ParentProductCard(
    product: ProductEntity,
    filteredProducts:List<ProductEntity>,
    cartViewModel: CartViewModel,
    tableViewModel: PosTableViewModel,
    tableNo: String,
    sessionId: String,
    currencyCode: String,
    localeTag: String,
    onProductAdded: () -> Unit
) {
 //   Log.d("MOD_DEBUG", "Loaded for baseId=$product.id ")
    var showVariantDialog by remember { mutableStateOf(false) }
    var showModifierDialog by remember { mutableStateOf(false) }
    val cartItems by cartViewModel.cart.collectAsState()
    val currentQty = cartItems
        .filter { it.tableId == tableNo && it.productId == product.id }
        .sumOf { it.quantity }

    val variants = remember(filteredProducts, product.id) {
        filteredProducts.filter { it.parentId == product.id }
    }

    val productBg = MaterialTheme.colorScheme.background//MaterialTheme.colorScheme.surface
   // val productText = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
    val productText = PosTheme.product.productCardText

    val addBg = PosTheme.accent.cartAddBg
    val addText = PosTheme.accent.cartAddText

    val removeBorder = PosTheme.accent.cartRemoveBorder
    val removeText = PosTheme.accent.cartRemoveText

    val context = LocalContext.current
    val db = AppDatabaseProvider.get(context)

    val modifierRepo = remember {
        ModifierRepository(db)
    }
    var modifierGroups by remember { mutableStateOf<List<ModifierGroupWithItems>>(emptyList()) }

    var selectedVariantId by remember { mutableStateOf<String?>(null) }


    val selectedSingle = remember { mutableStateMapOf<String, String>() }
    val selectedMulti = remember { mutableStateMapOf<String, MutableList<String>>() }


    // ✅ ADD IT HERE 👇 (RIGHT AFTER STATE)
    LaunchedEffect(showVariantDialog, showModifierDialog) {
        if (showVariantDialog || showModifierDialog) {
            selectedVariantId = null
            selectedSingle.clear()
            selectedMulti.clear()
        }
    }

    LaunchedEffect(selectedVariantId, showModifierDialog) {
        if (showModifierDialog || selectedVariantId != null) {
            val baseId = selectedVariantId ?: product.id
            modifierGroups = modifierRepo.getModifiersForProduct(product.id)

           // Log.d("MOD_DEBUG", "Loaded for baseId=$baseId size=${modifierGroups.size}")
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.20f) // light, subtle border
                )
            ),

        color = productBg,
        shape = RectangleShape
    ) {

        Column(
            modifier = Modifier
                .padding(11.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            val price = when {
                product.discountPrice == null || product.discountPrice == 0.0 -> product.price
                else -> product.discountPrice
            }

            val code = product.searchCode?.trim()
            val numericCode = code?.takeIf { it.all { ch -> ch.isDigit() } }

            val displayName = numericCode?.let {
                "${product.name} $it"
            } ?: product.name

            Text(
                text = toTitleCase(displayName),
                minLines = 2,
                maxLines = 2,
                lineHeight = 18.sp,
                color = productText
            )

//            Text(
//                text = "${product.taxRate}",
//                color = Color.White,
//                fontSize = 15.sp,
//                fontWeight = FontWeight.Bold
//            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    MoneyFormatter.format(
                        amount = price,
                        currencyCode = currencyCode,
                        localeTag = localeTag
                    ),
                    color = productText,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // 🔒 LEFT SIDE: fixed container so layout never shifts
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // ➖ Remove slot (always reserves space)
                    Box(
                        modifier = Modifier.size(width = 48.dp, height = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentQty > 0) {
                            IconButton(
                                onClick = { cartViewModel.decrease(product.id, tableNo) },
                                modifier = Modifier
                                    .size(width = 38.dp, height = 30.dp)
                                    .background(
                                        color = Color(0xFF64748B), // darker slate (not white)
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Text(
                                    "−",
                                    color = Color(0xFF1E293B), // dark slate text
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }


                        }
                    }

                    // 🔢 Qty slot (fixed width)
                    Box(
                        modifier = Modifier.size(width = 32.dp, height = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentQty > 0) {
                            Text(
                                currentQty.toString(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = productText
                            )
                        }
                    }
                }

                // ➕ Add (unchanged behavior)

                IconButton(
                    onClick = {

                        when {
                            // ✅ 1. Product has variants → open variant dialog
                            product.hasVariants == true -> {
                                showVariantDialog = true
                            }

                            // ✅ 2. Product has modifiers → open modifier dialog
//                            modifierGroups.isNotEmpty() -> {
//                                showModifierDialog = true
//                            }
                            product.hasModifiers == true -> {
                                showModifierDialog = true
                            }


                            // ✅ 3. Simple product → add directly
                            else -> {
                                cartViewModel.addProductToCart(
                                    product = product,
                                    price = price
                                )
                                onProductAdded()
                            }
                        }
                    },
                    modifier = Modifier
                        .size(width = 38.dp, height = 30.dp)
                        .background(
                            color = addBg.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(8.dp) // 👈 perfect POS feel
                        )
                ) {
                    Text(
                        "+",
                        color = addText,
                        fontSize = 18.sp
                    )
                }

            }

        }
    }


    if (showVariantDialog || showModifierDialog) {



        // 🔥 Base product (variant OR parent)
        val baseProduct = variants.find { it.id == selectedVariantId } ?: product

        // 🔁 Load modifiers based on selected variant OR product
        val baseId = selectedVariantId ?: product.id

        val selectedModifiersList = mutableListOf<CartModifier>()


        Dialog(
            onDismissRequest = {
                showVariantDialog = false
                showModifierDialog = false
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.95f),
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(0.dp)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {

                    // 🔥 TITLE
                    Text(
                        text = "Customize ${product.name}",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 🔥 CONTENT (SCROLLABLE)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {

                        // =========================
                        // ✅ VARIANT SECTION
                        // =========================
                        if (product.hasVariants == true && variants.isNotEmpty()) {

                            Text(
                                text = "Select Variant",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )

                            variants.forEach { variant ->

                                val variantPrice =
                                    if (variant.discountPrice == null || variant.discountPrice == 0.0)
                                        variant.price
                                    else variant.discountPrice

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    RadioButton(
                                        selected = selectedVariantId == variant.id,
                                        onClick = { selectedVariantId = variant.id }
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(variant.name, color = Color.White)
                                        Text(
                                            "₹$variantPrice",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                        }

                        // =========================
                        // ✅ MODIFIER SECTION
                        // =========================
                        if (modifierGroups.isEmpty()) {
                            Text("No customization available", color = Color.White)
                        } else {

                            modifierGroups.forEach { group ->

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF334155)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {

                                        Text(
                                            text = group.group.name,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        LazyVerticalGrid(
                                            columns = GridCells.Fixed(3),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 300.dp), // prevents infinite height crash
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            items(group.items.size) { index ->

                                                val item = group.items[index]
                                                val isSingle = group.group.maxSelection == 1

                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = Color(0xFF475569)
                                                    )
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(8.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {

                                                        if (isSingle) {
                                                            RadioButton(
                                                                selected = selectedSingle[group.group.id] == item.id,
                                                                onClick = {
                                                                    selectedSingle[group.group.id] = item.id
                                                                }
                                                            )
                                                        } else {

                                                            val selectedList =
                                                                selectedMulti.getOrPut(group.group.id) { mutableStateListOf<String>() }

                                                            Checkbox(
                                                                checked = selectedList.contains(item.id),
                                                                onCheckedChange = { isChecked ->

                                                                    if (isChecked) {
                                                                        if (selectedList.size < group.group.maxSelection) {
                                                                            selectedList.add(item.id)
                                                                        }
                                                                    } else {
                                                                        selectedList.remove(item.id)
                                                                    }
                                                                }
                                                            )
                                                        }

                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(item.name, color = Color.White)
                                                            Text(
                                                                "₹${item.price}",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = Color.LightGray
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // =========================
                        // 💰 LIVE TOTAL
                        // =========================
                        val extraPrice =
                            selectedSingle.values.sumOf { id ->
                                modifierGroups.flatMap { it.items }
                                    .find { it.id == id }?.price ?: 0.0
                            } +
                                    selectedMulti.values.flatten().sumOf { id ->
                                        modifierGroups.flatMap { it.items }
                                            .find { it.id == id }?.price ?: 0.0
                                    }

                        val total = baseProduct.price + extraPrice

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Total: ₹$total",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }

                    // 🔥 ADD BUTTON (STICKY)
                    val isVariantRequired = product.hasVariants == true
                    val isVariantSelected = selectedVariantId != null
                    val context = LocalContext.current


                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        // ❌ CANCEL BUTTON
                        OutlinedButton(
                            onClick = {
                                showVariantDialog = false
                                showModifierDialog = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        // ➕ ADD BUTTON
                        Button(
                            onClick = {

                                // 🚨 VALIDATION
                                if (product.hasVariants == true && selectedVariantId == null) {
                                    Toast.makeText(context, "Please select a variant", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                var extraPrice = 0.0
                                val selectedModifiersList = mutableListOf<CartModifier>()

                                modifierGroups.forEach { group ->

                                    val selectedItems = mutableListOf<CartModifierItem>()

                                    selectedSingle[group.group.id]?.let { itemId ->
                                        val item = group.items.find { it.id == itemId }
                                        if (item != null) {
                                            extraPrice += item.price
                                            selectedItems.add(
                                                CartModifierItem(item.id, item.name, item.price)
                                            )
                                        }
                                    }

                                    selectedMulti[group.group.id]?.forEach { itemId ->
                                        val item = group.items.find { it.id == itemId }
                                        if (item != null) {
                                            extraPrice += item.price
                                            selectedItems.add(
                                                CartModifierItem(item.id, item.name, item.price)
                                            )
                                        }
                                    }

                                    if (selectedItems.isNotEmpty()) {
                                        selectedItems.sortBy { it.itemId }

                                        selectedModifiersList.add(
                                            CartModifier(
                                                group.group.id,
                                                group.group.name,
                                                selectedItems
                                            )
                                        )
                                    }
                                }

                                selectedModifiersList.sortBy { it.groupId }

                                val modifiersJson =
                                    ModifierJsonHelper.toJson(selectedModifiersList)

                                cartViewModel.addProductToCart(
                                    product = baseProduct,
                                    price = baseProduct.price,
                                    modifiersJson = modifiersJson
                                )

                                showVariantDialog = false
                                showModifierDialog = false
                                onProductAdded()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Add")
                        }
                    }
                }
            }
        }
    }
}
