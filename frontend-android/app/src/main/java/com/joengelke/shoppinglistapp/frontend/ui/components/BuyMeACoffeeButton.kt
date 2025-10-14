package com.joengelke.shoppinglistapp.frontend.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri

@Composable
fun BuyMeACoffeeButton(context: Context) {
    Button(
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW, "https://www.buymeacoffee.com/jengelke".toUri())
            context.startActivity(intent)
        },
        modifier = Modifier
            .padding(16.dp)
    ) {
        Text("☕ Buy Me a Coffee")
    }
}