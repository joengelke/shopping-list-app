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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.joengelke.shoppinglistapp.frontend.models.ShoppingItem
import com.joengelke.shoppinglistapp.frontend.viewmodel.ShoppingItemsViewModel
import kotlinx.coroutines.delay
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingItemsCreateScreen(
    navController: NavController,
    shoppingListId: String,
    shoppingItemsViewModel: ShoppingItemsViewModel = hiltViewModel()
) {

    var name by remember { mutableStateOf("") }

    val newShoppingItem = listOf(ShoppingItem(
        id = "",
        name = name,
        category = "",
        amount = 0.0,
        unit = "",
        checked = false,
        note = "",
        editedAt = Date(),
        editedBy = ""
    ))

    val shoppingItems by shoppingItemsViewModel.shoppingItems.collectAsState()

    //TODO get all Possible Items from Backend once its Screen is loaded

    val tmpOwnSuggestionsList = listOf(
        ShoppingItem(
            id = "",
            name = "Apple",
            category = "",
            amount = 0.0,
            unit = "",
            checked = false,
            note = "",
            editedAt = Date(),
            editedBy = ""
        ),
        ShoppingItem(
            id = "",
            name = "Banana",
            category = "",
            amount = 0.0,
            unit = "",
            checked = false,
            note = "",
            editedAt = Date(),
            editedBy = ""
        ),
        ShoppingItem(
            id = "",
            name = "Bread",
            category = "",
            amount = 0.0,
            unit = "",
            checked = false,
            note = "",
            editedAt = Date(),
            editedBy = ""
        ),
        ShoppingItem(
            id = "",
            name = "Butter",
            category = "",
            amount = 0.0,
            unit = "",
            checked = false,
            note = "",
            editedAt = Date(),
            editedBy = ""
        ),
        ShoppingItem(
            id = "",
            name = "Carrot",
            category = "",
            amount = 0.0,
            unit = "",
            checked = false,
            note = "",
            editedAt = Date(),
            editedBy = ""
        ),
        ShoppingItem(
            id = "",
            name = "Cheese",
            category = "",
            amount = 0.0,
            unit = "",
            checked = false,
            note = "",
            editedAt = Date(),
            editedBy = ""
        ),
        ShoppingItem(
            id = "",
            name = "Chicken",
            category = "",
            amount = 0.0,
            unit = "",
            checked = false,
            note = "",
            editedAt = Date(),
            editedBy = ""
        )
    )
    val ownSuggestionsList = tmpOwnSuggestionsList.filter{it.name !in shoppingItems.map{it.name}.toSet()}

    // Filter items that start with the entered name
    val filteredShoppingItems = shoppingItems.filter { it.name.startsWith(name, ignoreCase = true) }
    val filteredOwnSuggestions =
        ownSuggestionsList.filter { it.name.startsWith(name, ignoreCase = true) }

    // own item name always first item (if it doesn't exists), then all suggestions are added
    val allItems =
        if (name.isNotEmpty()) {
            if (!(filteredShoppingItems+filteredOwnSuggestions).any{it.name == name}) {
                newShoppingItem + filteredShoppingItems + filteredOwnSuggestions
            } else {
                filteredShoppingItems + filteredOwnSuggestions
            }
        } else {
            emptyList()
        }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current


    LaunchedEffect(Unit) {
        shoppingItemsViewModel.loadShoppingItems(shoppingListId, onSuccess = {})
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

                items(allItems) { item ->
                    OwnAndSuggestionItemContainer(item.name, item.amount,
                        addOneItem = {
                            shoppingItemsViewModel.addOneShoppingItem(
                                shoppingListId,
                                item.name
                            )
                        },
                        removeOneItem = {
                            shoppingItemsViewModel.removeOneShoppingItem(
                                shoppingListId,
                                item.id
                            )
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun OwnAndSuggestionItemContainer(
    name: String,
    amount: Double,
    addOneItem: (String) -> Unit,
    removeOneItem: (String) -> Unit
) {
    var isAdded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp),
        colors = CardDefaults.cardColors(if (amount >= 1) Color.Green.copy(alpha = 0.3f) else Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    //amount += 1
                    addOneItem(name)
                    isAdded = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
            Text(
                text = name,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (amount > 0) {
                    Text(
                        text = amount.let {
                            if (it % 1 == 0.0) it.toInt().toString() else it.toString()
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                    IconButton(
                        onClick = {
                            //amount -= 1
                            removeOneItem(name)
                        }
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Add Item")
                    }
                }
            }

        }
    }
}