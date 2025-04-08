package com.joengelke.shoppinglistapp.frontend.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.joengelke.shoppinglistapp.frontend.R
import com.joengelke.shoppinglistapp.frontend.models.ShoppingItem
import com.joengelke.shoppinglistapp.frontend.viewmodel.ShoppingItemsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingItemsCreateScreen(
    navController: NavController,
    shoppingListId: String,
    shoppingItemsViewModel: ShoppingItemsViewModel = hiltViewModel()
) {

    var name by remember { mutableStateOf("") }

    val newShoppingItem = listOf(
        ShoppingItem(
            id = "",
            name = name,
            category = "",
            amount = 0.0,
            unit = "",
            checked = true,
            note = "",
            editedAt = "",
            editedBy = ""
        )
    )

    val shoppingItems by shoppingItemsViewModel.shoppingItems.collectAsState()

    //TODO get all Possible Items from Backend once its Screen is loaded

    /*
    val tmpOwnSuggestionsList = listOf(
        ShoppingItem(
            id = "",
            name = "Apple",
            category = "",
            amount = 0.0,
            unit = "",
            checked = false,
            note = "",
            editedAt = "",
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
            editedAt = "",
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
            editedAt = "",
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
            editedAt = "",
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
            editedAt = "",
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
            editedAt = "",
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
            editedAt = "",
            editedBy = ""
        )
    )


    val ownSuggestionsList =
        tmpOwnSuggestionsList.filter { it.name !in shoppingItems.map { it.name }.toSet()

    val filteredOwnSuggestions =
        ownSuggestionsList.filter { it.name.contains(name, ignoreCase = true) }

     */

    // Filter items that start with the entered name
    val filteredShoppingItems = shoppingItems.filter { it.name.contains(name, ignoreCase = true) }

    // own item name always first item (if it doesn't exists), then all suggestions are added
    val allItems =
        if (name.isNotEmpty()) {
            if (!filteredShoppingItems.any { it.name == name }) {
                newShoppingItem + filteredShoppingItems
            } else {
                filteredShoppingItems
            }
        } else {
            // TODO add other sorting parameters
            filteredShoppingItems.sortedBy { it.name }
        }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Pager
    val pagerState = rememberPagerState(pageCount = { 2 })
    val tabTitles = listOf("Items", "Sets")
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        shoppingItemsViewModel.loadShoppingItems(shoppingListId, onSuccess = {})
        delay(300)
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Scaffold(
        topBar = {
            TopAppBar(modifier = Modifier.padding(horizontal = 8.dp),
                title = {
                    OutlinedTextField(
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            text = { Text(title, fontWeight = FontWeight.Bold) },
                            selected = selectedTabIndex == index,
                            onClick = {
                                selectedTabIndex = index
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                        )
                    }
                }

                // Items and Sets pages
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { page ->
                    when (page) {
                        0 -> {
                            ShoppingItemPage(allItems,
                                addOneItem = { addItem ->
                                    shoppingItemsViewModel.addOneShoppingItem(
                                        shoppingListId,
                                        addItem
                                    )
                                },
                                removeOneItem = { itemId ->
                                    shoppingItemsViewModel.removeOneShoppingItem(
                                        itemId
                                    )
                                }
                            )
                        }

                        1 -> {
                            ItemSetsPage()
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun ShoppingItemPage(
    shoppingItemList: List<ShoppingItem>,
    addOneItem: (ShoppingItem) -> Unit,
    removeOneItem: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {

        items(shoppingItemList) { item ->
            ItemContainer(item,
                addOneItem = { addItem ->
                    addOneItem(addItem)
                },
                removeOneItem = { itemId ->
                    removeOneItem(itemId)
                }
            )
        }
    }
}

@Composable
fun ItemContainer(
    item: ShoppingItem,
    addOneItem: (ShoppingItem) -> Unit,
    removeOneItem: (String) -> Unit
) {
    var isAdded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp),
        colors = CardDefaults.cardColors(
            if (!item.checked) Color.Green.copy(
                alpha = 0.3f
            ) else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Add item button
            IconButton(
                onClick = {
                    addOneItem(item)
                    isAdded = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
            Text(
                text = item.name,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!item.checked) {
                    if (item.amount>0) {
                        Text(
                            text = item.amount.let {
                                if (it % 1 == 0.0) it.toInt().toString() else it.toString()
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            //amount -= 1
                            removeOneItem(item.id)
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_remove_24),
                            contentDescription = "Remove"
                        )
                    }
                }
            }

        }
    }
}

@Composable
fun ItemSetsPage(
    //shoppingItemSets: List<List<ShoppingItem>>
) {
    Text(text = "TODO")
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {


    }
}