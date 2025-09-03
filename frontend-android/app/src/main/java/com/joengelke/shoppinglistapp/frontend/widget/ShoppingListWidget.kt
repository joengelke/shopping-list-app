package com.joengelke.shoppinglistapp.frontend.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.joengelke.shoppinglistapp.frontend.MainActivity
import com.joengelke.shoppinglistapp.frontend.datastore.LoginDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ShoppingListWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val loggedIn = LoginDataStore.isLoggedInFlow(context).first() // read current state

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (loggedIn) "Welcome back!" else "Please log in",
                        style = TextStyle(fontSize = 18.sp)
                    )

                    Spacer(modifier = GlanceModifier.height(8.dp))

                    Button(
                        text = if (loggedIn) "Open App" else "Login",
                        onClick = actionStartActivity<MainActivity>()
                    )

                    Spacer(modifier = GlanceModifier.height(16.dp))

                    Button(
                        text = "Refresh",
                        onClick = actionRunCallback<RefreshWidgetAction>()
                    )
                }
            }
        }
    }

    class RefreshWidgetAction : ActionCallback {
        override suspend fun onAction(
            context: Context,
            glanceId: GlanceId,
            parameters: ActionParameters
        ) {
            // It's good practice to run potentially long-running work or I/O
            // off the main thread, though updateAll itself might handle this.
            withContext(Dispatchers.IO) {
                refresh(context)
            }
        }
    }

    companion object {
        // Trigger widget update
        suspend fun refresh(context: Context) {
            val glanceAppWidget = ShoppingListWidget()
            glanceAppWidget.updateAll(context)
        }
    }
}