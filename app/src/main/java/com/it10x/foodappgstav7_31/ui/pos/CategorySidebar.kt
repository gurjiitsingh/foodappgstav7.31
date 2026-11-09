package com.it10x.foodappgstav7_31.ui.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_31.data.pos.entities.CategoryEntity
import com.it10x.foodappgstav7_31.ui.theme.PosTheme
import com.it10x.foodappgstav7_31.data.pos.viewmodel.FAVORITES_CATEGORY_ID
@Composable
fun CategorySidebar(
    categories: List<CategoryEntity>,
    selectedCatId: String?,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .widthIn(min = 90.dp, max = 130.dp),
        tonalElevation = 6.dp,
        color = Color.Transparent,
        shape = RoundedCornerShape(0.dp)
    ) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(top = 13.dp, start = 6.dp, end = 6.dp)
        ) {

            // ⭐ FAVORITES
            item {

                val isSelected =
                    selectedCatId == FAVORITES_CATEGORY_ID

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onCategorySelected(FAVORITES_CATEGORY_ID)
                        },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected)
                        PosTheme.category.selectedBg
                    else
                        PosTheme.category.unselectedBg,
                    tonalElevation = if (isSelected) 4.dp else 1.dp
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 8.dp,
                                vertical = 12.dp
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = "⭐ Favorites",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected)
                                FontWeight.SemiBold
                            else
                                FontWeight.Normal,
                            color = if (isSelected)
                                PosTheme.category.selectedText
                            else
                                PosTheme.category.unselectedText,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // NORMAL CATEGORIES

            items(categories) { category ->

                val isSelected = selectedCatId == category.id

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCategorySelected(category.id) },
                    shape = RoundedCornerShape(10.dp),

                    color = if (isSelected)
                        PosTheme.category.selectedBg
                    else
                        PosTheme.category.unselectedBg,

                    tonalElevation = if (isSelected) 4.dp else 1.dp // 👈 slight lift for non-selected
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected)
                                FontWeight.SemiBold
                            else
                                FontWeight.Normal,

                            color = if (isSelected)
                                PosTheme.category.selectedText
                            else
                                PosTheme.category.unselectedText,

                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}