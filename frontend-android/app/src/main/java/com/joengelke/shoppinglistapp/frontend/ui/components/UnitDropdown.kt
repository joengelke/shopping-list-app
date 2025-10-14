package com.joengelke.shoppinglistapp.frontend.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.joengelke.shoppinglistapp.frontend.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitDropdown(
    unit: String,
    onUnitSelected: (String) -> Unit,
    withLabel: Boolean,
    modifier: Modifier = Modifier
) {
    val units = listOf(
        "", "ml", "l", "g", "kg",
        stringResource(R.string.teaspoon),
        stringResource(R.string.tablespoon),
        stringResource(R.string.pinch),
        stringResource(R.string.pieces_short),
        stringResource(R.string.cup),
        stringResource(R.string.cube),
        stringResource(R.string.pck)
    )

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = unit.ifEmpty { " " },
            onValueChange = {},
            label = if (withLabel){ { Text(stringResource(R.string.unit))} } else null,
            readOnly = true,
            enabled = false,
            singleLine = true,
            trailingIcon = if (withLabel){{
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }} else null,
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, expanded)
                .clickable { expanded = true },
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = MaterialTheme.colorScheme.primary,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            units.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onUnitSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}