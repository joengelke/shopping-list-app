package com.joengelke.shoppinglistapp.frontend.ui.screens.shoppinglist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.joengelke.shoppinglistapp.frontend.R
import com.joengelke.shoppinglistapp.frontend.viewmodel.ShoppingListViewModel


@Composable
fun ShoppingListCreateScreen(
    navController: NavHostController,
    shoppingListViewModel: ShoppingListViewModel = hiltViewModel()
) {
    var shoppingListName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.create_shopping_list),
            style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Shopping List Name TextField
        OutlinedTextField(
            value = shoppingListName,
            onValueChange = { shoppingListName = it },
            label = { Text(stringResource(R.string.shopping_list_name)) },
            isError = errorMessage != null,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
                .focusRequester(focusRequester)
        )

        // TODO restructure error Message
        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)

        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (shoppingListName.isNotBlank()) {
                    shoppingListViewModel.createShoppingList(
                        name = shoppingListName.trim(),
                        onSuccess = {
                            navController.popBackStack()
                        }
                    )
                } else {
                    errorMessage = context.getString(R.string.please_enter_a_name)
                }
            }
        ) {
            Text(text = stringResource(R.string.create_shopping_list))
        }
    }
}