package com.joengelke.shoppinglistapp.frontend.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.joengelke.shoppinglistapp.frontend.R
import com.joengelke.shoppinglistapp.frontend.models.ItemSet
import com.joengelke.shoppinglistapp.frontend.navigation.Routes
import com.joengelke.shoppinglistapp.frontend.viewmodel.ItemSetsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemSetOverviewScreen(
    navController: NavHostController,
    shoppingListId: String,
    itemSetsViewModel: ItemSetsViewModel = hiltViewModel()
) {
    val itemSets by itemSetsViewModel.itemSets.collectAsState()

    var refreshing by remember { mutableStateOf(false) }
    val state = rememberPullToRefreshState()
    val onRefresh: () -> Unit = {
        refreshing = true
        itemSetsViewModel.loadItemSets(
            shoppingListId,
            onSuccess = { refreshing = false }
        )
    }

    var showDialog by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(shoppingListId) {
        itemSetsViewModel.loadItemSets(shoppingListId, onSuccess = {})
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Item Sets",
                            fontSize = 24.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.DarkGray
                ),
                actions = {
                    // dropdown menu possible
                }
            )
        },
        content = { paddingValues ->
            PullToRefreshBox(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                state = state,
                isRefreshing = refreshing,
                onRefresh = onRefresh
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(itemSets) { itemSet ->
                        ItemSetContainer(
                            itemSet = itemSet,
                            onEditItems = { itemSetId ->
                                navController.navigate(
                                    Routes.ItemSetCreate.createRoute(
                                        shoppingListId,
                                        itemSetId
                                    )
                                )
                            },
                            onEdit = { updatedItemSet ->
                                itemSetsViewModel.updateItemSet(
                                    shoppingListId,
                                    updatedItemSet,
                                    onSuccess = {}
                                )
                            },
                            onDelete = { itemSetId ->
                                itemSetsViewModel.deleteItemSet(shoppingListId, itemSetId)
                            }
                        )
                    }
                    item {
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            onClick = {
                                showDialog = true
                            }
                        ) {
                            Text("New Item Set")
                        }
                    }
                }
            }
        }
    )

    if (showDialog) {
        var itemSetName by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        Dialog(
            onDismissRequest = { showDialog = false }
        ) {
            Card(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = itemSetName,
                        onValueChange = { itemSetName = it },
                        placeholder = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        )
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            showDialog = false
                            itemSetName = ""
                        }
                    ) {
                        Text("Cancel", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (itemSetName.isNotBlank()) {
                                itemSetsViewModel.createItemSet(
                                    shoppingListId,
                                    itemSetName,
                                    onSuccess = { itemSet ->
                                        navController.navigate(
                                            Routes.ItemSetCreate.createRoute(
                                                shoppingListId,
                                                itemSet.id
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    ) {
                        Text("Create", fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemSetContainer(
    itemSet: ItemSet,
    onEditItems: (String) -> Unit,
    onEdit: (ItemSet) -> Unit,
    onDelete: (String) -> Unit
) {
    var delete by remember { mutableStateOf(false) }
    var edit by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(itemSet.name) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clickable {
                onEditItems(itemSet.id)
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {

            if (!edit) {
                Text(
                    text = itemSet.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(20.dp)
                        .weight(1f)
                )
                IconButton(
                    onClick = {
                        edit = true
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_edit_24),
                        contentDescription = "edit item set name",
                        tint = Color.Black
                    )
                }
                IconButton(
                    onClick = {
                        delete = true
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_delete_24),
                        contentDescription = "delete item set",
                        tint = Color.Black
                    )
                }
            } else {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                    },
                    modifier = Modifier
                        .padding(start = 12.dp, top = 4.dp, bottom = 4.dp, end = 8.dp)
                        .weight(1f)
                )
                IconButton(
                    onClick = {
                        onEdit(
                            itemSet.copy(name = name),
                        )
                        edit = false
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_check_24),
                        contentDescription = "save item set name",
                        tint = Color.Black
                    )
                }
                IconButton(
                    onClick = {
                        edit = false
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_close_24),
                        contentDescription = "cancel edit",
                        tint = Color.Black
                    )
                }

            }
        }
    }
    if (delete) {
        BasicAlertDialog(
            onDismissRequest = { delete = false },
            content = {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Delete Item Set?",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    delete = false
                                },
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = {
                                    delete = false
                                    onDelete(itemSet.id)
                                },
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        )
    }
}
