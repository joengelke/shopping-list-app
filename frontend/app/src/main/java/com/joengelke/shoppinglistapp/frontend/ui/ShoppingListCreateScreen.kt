package com.joengelke.shoppinglistapp.frontend.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.joengelke.shoppinglistapp.frontend.viewmodel.ShoppingListViewModel


@Composable
fun ShoppingListCreateScreen(
    navController: NavController,
    shoppingListViewModel: ShoppingListViewModel = hiltViewModel()
) {
    var shoppingListName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Create Shopping List", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Shopping List Name TextField
        OutlinedTextField(
            value = shoppingListName,
            onValueChange = { shoppingListName = it },
            label = { Text("Shopping List Name") },
            isError = errorMessage != null
        )

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)

        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if(shoppingListName.isNotBlank()) {
                    shoppingListViewModel.createShoppingList(
                        name = shoppingListName,
                        onSuccess = {
                            navController.popBackStack()
                        },
                        onError = {errorMessage = "Failed to create shopping list"}
                    )
                } else {
                    errorMessage = "Please enter a name"
                }
            }
        ) {
            Text(text="Create shopping list")
        }
    }
}