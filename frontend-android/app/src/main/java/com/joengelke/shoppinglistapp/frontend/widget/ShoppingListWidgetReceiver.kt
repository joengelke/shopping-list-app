package com.joengelke.shoppinglistapp.frontend.widget

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.joengelke.shoppinglistapp.frontend.utils.AppActions
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ShoppingListWidgetReceiver: GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ShoppingListWidget()

    private val coroutineScope = MainScope()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent) // Important to call super

        // Check if the received intent is the one we're interested in
        if (intent.action == AppActions.ACTION_LOGIN_STATUS_CHANGED) {
            // Launch a coroutine to call the suspend function refresh()
            coroutineScope.launch {
                ShoppingListWidget.refresh(context)
            }
        }
    }
}