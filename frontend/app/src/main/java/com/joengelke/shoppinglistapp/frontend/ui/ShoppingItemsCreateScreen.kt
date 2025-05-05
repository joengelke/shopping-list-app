package com.joengelke.shoppinglistapp.frontend.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.joengelke.shoppinglistapp.frontend.R
import com.joengelke.shoppinglistapp.frontend.models.ItemSet
import com.joengelke.shoppinglistapp.frontend.models.ItemSetItem
import com.joengelke.shoppinglistapp.frontend.models.ShoppingItem
import com.joengelke.shoppinglistapp.frontend.viewmodel.ItemSetsViewModel
import com.joengelke.shoppinglistapp.frontend.viewmodel.ShoppingItemsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingItemsCreateScreen(
    navController: NavHostController,
    shoppingListId: String,
    shoppingItemsViewModel: ShoppingItemsViewModel = hiltViewModel(),
    itemSetsViewModel: ItemSetsViewModel = hiltViewModel()
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
    val itemSets by itemSetsViewModel.itemSets.collectAsState()

    /*
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
    val focusManager = LocalFocusManager.current

    // Pager
    val pagerState = rememberPagerState(pageCount = { 2 })
    val tabTitles = listOf("Items", "Sets")
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        shoppingItemsViewModel.loadShoppingItems(shoppingListId, onSuccess = {})
        itemSetsViewModel.loadItemSets(shoppingListId, onSuccess = {})
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    LaunchedEffect(name) {
        if (name.isNotEmpty() && pagerState.currentPage != 0) {
            pagerState.animateScrollToPage(0)
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = {
                                Text(
                                    "Enter item name",
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            },
                            trailingIcon = {
                                if (name.isNotBlank()) {
                                    IconButton(
                                        onClick = { name = "" },
                                        modifier = Modifier.padding(end =4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(50),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.onPrimary,
                                focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(
                                    alpha = 0.5f
                                ),
                                unfocusedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                cursorColor = MaterialTheme.colorScheme.onPrimary,
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )


            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pointerInput(Unit) {
                    detectTapGestures{ focusManager.clearFocus()}
                }
        ) {
            AnimatedVisibility(name.isEmpty()) {
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            text = { Text(title, fontWeight = FontWeight.Bold) },
                            selected = pagerState.currentPage == index,
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                        )
                    }
                }
            }

            // Items and Sets pages
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                userScrollEnabled = name.isEmpty()
            ) { page ->
                when (page) {
                    0 -> {
                        ShoppingItemPage(
                            allItems,
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
                        ItemSetsPage(
                            shoppingItems,
                            itemSets,
                            addItemSetItemToShoppingList = { addItemSetItem ->
                                shoppingItemsViewModel.addItemSetItemToShoppingList(
                                    addItemSetItem
                                )
                            },
                            removeItemSetItemFromShoppingList = { removeItemSetItem ->
                                shoppingItemsViewModel.removeItemSetItemFromShoppingList(
                                    removeItemSetItem
                                )
                            },
                            addAllItemSetItemsToShoppingList = { itemSetId ->
                                shoppingItemsViewModel.addAllItemSetItemsToShoppingList(
                                    itemSetId
                                )
                            },
                            removeAllItemSetItemsFromShoppingList = { itemSetId ->
                                shoppingItemsViewModel.removeAllItemSetItemsFromShoppingList(
                                    itemSetId
                                )
                            }
                        )
                    }
                }
            }
        }
    }
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
            ItemContainer(
                item,
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
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RectangleShape,
        colors =
            if (!item.checked) {
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            }
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
                Icon(
                    painter = painterResource(id = R.drawable.baseline_add_24),
                    contentDescription = "Add Item"
                )
            }
            Text(
                text = item.name,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!item.checked) {
                    if (item.amount > 0) {
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
    HorizontalDivider(
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    )
}

@Composable
fun ItemSetsPage(
    shoppingItemList: List<ShoppingItem>,
    shoppingItemSets: List<ItemSet>,
    addItemSetItemToShoppingList: (ItemSetItem) -> Unit,
    removeItemSetItemFromShoppingList: (ItemSetItem) -> Unit,
    addAllItemSetItemsToShoppingList: (String) -> Unit,
    removeAllItemSetItemsFromShoppingList: (String) -> Unit
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(shoppingItemSets) { itemSet ->
            ItemSetContainer(
                shoppingItemList,
                itemSet,
                addItemSetItemToShoppingList = { addItemSetItem ->
                    addItemSetItemToShoppingList(addItemSetItem)
                },
                removeItemSetItemFromShoppingList = { removeItemSetItem ->
                    removeItemSetItemFromShoppingList(removeItemSetItem)
                },
                addAllItemSetItemsToShoppingList = { itemSetId ->
                    addAllItemSetItemsToShoppingList(itemSetId)
                },
                removeAllItemSetItemsFromShoppingList = { itemSetId ->
                    removeAllItemSetItemsFromShoppingList(itemSetId)
                }
            )
        }
    }
}

@Composable
fun ItemSetContainer(
    shoppingItemList: List<ShoppingItem>,
    itemSet: ItemSet,
    addItemSetItemToShoppingList: (ItemSetItem) -> Unit,
    removeItemSetItemFromShoppingList: (ItemSetItem) -> Unit,
    addAllItemSetItemsToShoppingList: (String) -> Unit,
    removeAllItemSetItemsFromShoppingList: (String) -> Unit
) {
    var itemSetFolded by remember { mutableStateOf(true) }

    var allAdded by remember { mutableStateOf(false) }
    val itemSetItemStates =
        remember { mutableStateMapOf<String, Boolean>() } // creates added state for each ItemSetItem

    fun updateAllAdded() {
        allAdded = itemSet.itemList.all { itemSetItem ->
            itemSetItemStates[itemSetItem.id] == true
        }
    }

    LaunchedEffect(itemSet.itemList) {
        itemSet.itemList.forEach { itemSetItem ->
            itemSetItemStates.putIfAbsent(itemSetItem.tmpId, allAdded)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RectangleShape,
        colors =
            if (allAdded) {
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { itemSetFolded = !itemSetFolded }
                .padding(start = 2.dp, end = 16.dp, bottom = 8.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (!allAdded) {
                        addAllItemSetItemsToShoppingList(itemSet.id)
                        itemSet.itemList.forEach { itemSetItem ->
                            itemSetItemStates[itemSetItem.id] = true
                        }
                        itemSetFolded = false
                        allAdded = true
                    } else {
                        removeAllItemSetItemsFromShoppingList(itemSet.id)
                        itemSet.itemList.forEach { itemSetItem ->
                            itemSetItemStates[itemSetItem.id] = false
                        }
                        itemSetFolded = true
                        allAdded = false
                    }
                }
            ) {
                Icon(
                    painter = painterResource(
                        id = if (!allAdded) {
                            R.drawable.baseline_playlist_add_24
                        } else {
                            R.drawable.baseline_playlist_remove_24
                        }
                    ),
                    contentDescription = if (!allAdded) "Add all" else "Remove all"
                )
            }
            Text(
                text = itemSet.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (itemSetFolded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Open item set"
            )

        }
    }

    if (!itemSetFolded) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            itemSet.itemList.forEach { itemSetItem ->
                val isAdded = itemSetItemStates[itemSetItem.id] ?: false
                ItemSetItemContainer(
                    shoppingItemList.find { it.id == itemSetItem.id }!!,
                    itemSetItem,
                    isAdded,
                    addItemSetItemToShoppingList = { addItemSetItem ->
                        addItemSetItemToShoppingList(addItemSetItem)
                        itemSetItemStates[itemSetItem.id] = true
                        updateAllAdded()
                    },
                    removeItemSetItemFromShoppingList = { removeItemSetItem ->
                        removeItemSetItemFromShoppingList(removeItemSetItem)
                        itemSetItemStates[itemSetItem.id] = false
                        allAdded = false
                    }
                )
            }
        }
    }
    HorizontalDivider(
        thickness = 2.dp,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    )
}

@Composable
fun ItemSetItemContainer(
    shoppingItem: ShoppingItem,
    itemSetItem: ItemSetItem,
    isAdded: Boolean,
    addItemSetItemToShoppingList: (ItemSetItem) -> Unit,
    removeItemSetItemFromShoppingList: (ItemSetItem) -> Unit
) {
    HorizontalDivider(
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    )
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RectangleShape,
        colors =
            if (isAdded) {
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isAdded) {
                IconButton(
                    onClick = {
                        addItemSetItemToShoppingList(itemSetItem)
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_add_24),
                        contentDescription = "Add"
                    )
                }
            }
            Text(
                text = itemSetItem.name,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (shoppingItem.amount > 0) {
                    Text(
                        text = "(${
                            shoppingItem.amount.let {
                                if (it % 1 == 0.0) it.toInt().toString() else it.toString()
                            }
                        }${shoppingItem.unit})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                }
                if (itemSetItem.amount > 0) {
                    Text(
                        text = "${
                            itemSetItem.amount.let {
                                if (it % 1 == 0.0) it.toInt().toString() else it.toString()
                            }
                        }${itemSetItem.unit}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                }
                if (isAdded) {
                    IconButton(
                        onClick = {
                            removeItemSetItemFromShoppingList(itemSetItem)
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