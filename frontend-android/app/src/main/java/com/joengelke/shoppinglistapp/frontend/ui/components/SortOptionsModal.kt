package com.joengelke.shoppinglistapp.frontend.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.joengelke.shoppinglistapp.frontend.R
import com.joengelke.shoppinglistapp.frontend.ui.common.SortCategory
import com.joengelke.shoppinglistapp.frontend.ui.common.SortDirection
import com.joengelke.shoppinglistapp.frontend.ui.common.SortOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : SortCategory> SortOptionsModal(
    title: String,
    currentSortOption: SortOptions<T>,
    availableCategories: List<T>,
    getCategoryLabel: @Composable (T) -> String,
    onChooseSortOption: (SortOptions<T>) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        shape = RectangleShape,
        modifier = Modifier.wrapContentHeight(),
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = title)
            }

            availableCategories.forEach { category ->
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )

                val isSelected = currentSortOption.category == category
                val direction = currentSortOption.direction

                ListItem(
                    headlineContent = {
                        Text(text = getCategoryLabel(category))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val newDirection = if (isSelected && direction == SortDirection.ASCENDING) {
                                SortDirection.DESCENDING
                            } else {
                                SortDirection.ASCENDING
                            }
                            onChooseSortOption(SortOptions(category, newDirection))
                        },
                    trailingContent = {
                        if (isSelected) {
                            Icon(
                                painter = painterResource(
                                    id = if (direction == SortDirection.ASCENDING)
                                        R.drawable.baseline_arrow_upward_24
                                    else
                                        R.drawable.baseline_arrow_downward_24
                                ),
                                contentDescription = if (direction == SortDirection.ASCENDING)
                                    "Ascending"
                                else
                                    "Descending"
                            )
                        }
                    }
                )
            }
        }
    }
}
