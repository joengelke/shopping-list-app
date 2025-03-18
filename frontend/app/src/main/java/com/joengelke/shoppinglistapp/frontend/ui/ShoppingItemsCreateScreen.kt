package com.joengelke.shoppinglistapp.frontend.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.joengelke.shoppinglistapp.frontend.models.ShoppingItemCreateRequest
import com.joengelke.shoppinglistapp.frontend.viewmodel.ShoppingItemsViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingItemsCreateScreen(
    navController: NavController,
    shoppingListId: String,
    shoppingItemsViewModel: ShoppingItemsViewModel = hiltViewModel()
) {

    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    //TODO get all Possible Items from Backend once its Screen is loaded
    val allPossibleItems = listOf(
        "Apple",
        "Banana",
        "Bread",
        "Butter",
        "Carrot",
        "Cheese",
        "Chicken"
    ) // Example suggestions
    val otherSuggestions = allPossibleItems.filter { it.startsWith(name, ignoreCase = true) }

    // typed in itemName always first item, then all suggestions are added
    val ownItemAndSuggestions =
        if (name.isNotEmpty()) listOf(name) + otherSuggestions else emptyList()

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = name,
                        onValueChange = { newName ->
                            run {
                                name = newName
                            }
                        },
                        placeholder = { Text("Enter item name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {

                items(ownItemAndSuggestions) { itemName ->
                    OwnAndSuggestionItemContainer(itemName, onAddItem = {
                        val newItem = ShoppingItemCreateRequest(itemName)
                        shoppingItemsViewModel.addShoppingItem(
                            shoppingListId,
                            newItem
                        )
                    })
                }
            }
        }
    )
}

@Composable
fun OwnAndSuggestionItemContainer(
    itemName: String,
    onAddItem: (String) -> Unit
) {
    var isAdded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp),
        colors = CardDefaults.cardColors(if (isAdded) Color.Green.copy(alpha = 0.3f) else Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    onAddItem(itemName)
                    isAdded = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
            Text(
                text = itemName,
                modifier = Modifier
                    .padding(start = 8.dp)
            )
        }
    }
}