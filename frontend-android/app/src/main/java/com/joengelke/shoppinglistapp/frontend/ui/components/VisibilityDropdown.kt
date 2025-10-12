package com.joengelke.shoppinglistapp.frontend.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.joengelke.shoppinglistapp.frontend.R
import com.joengelke.shoppinglistapp.frontend.models.Visibility
import com.joengelke.shoppinglistapp.frontend.viewmodel.RecipeViewModel

@Composable
fun VisibilityDropdown(
    recipeId: String,
    visibility: Visibility,
    iconColor: Color,
    recipeViewModel: RecipeViewModel = hiltViewModel(),
    onChange: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    val visibilityOptions = listOf(
        Triple(Visibility.PRIVATE, R.drawable.baseline_lock_24, stringResource(R.string.private_word)),
        //Triple(Visibility.SHARED, R.drawable.baseline_group_24, "Shared"), TODO
        Triple(Visibility.PUBLIC, R.drawable.baseline_public_24, stringResource(R.string.public_word))
    )

    val currentOption =
        visibilityOptions.find { it.first == visibility } ?: visibilityOptions.first()

    Box {
        IconButton(
            onClick = { expanded = true }
        ) {
            if (!loading) {
                Icon(
                    painter = painterResource(id = currentOption.second),
                    contentDescription = currentOption.third,
                    tint = iconColor
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp),
                    color = iconColor,
                    strokeWidth = 1.5.dp
                )
            }
        }


        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            visibilityOptions.forEach { (option, iconRes, label) ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = iconRes),
                                contentDescription = label,
                                modifier = Modifier.size(20.dp),
                                tint = if (option == visibility)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label)
                        }
                    },
                    onClick = {
                        loading = true
                        recipeViewModel.changeVisibility(
                            recipeId,
                            option,
                            onSuccess = {
                                loading = false
                                expanded = false
                                onChange()
                            }
                        )
                    }
                )
            }
        }
    }
}
