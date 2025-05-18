package com.joengelke.shoppinglistapp.frontend.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsContainer(
    text: String,
    icon:  @Composable() (() -> Unit)? = null,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(text = text)
        },
        trailingContent = {
            if(icon!=null) {
                icon()
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Select Container"
                )
            }
        },
        modifier = Modifier.clickable { onClick() }
    )
    HorizontalDivider(
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    )
}