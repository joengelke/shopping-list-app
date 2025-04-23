package com.joengelke.shoppinglistapp.frontend.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.joengelke.shoppinglistapp.frontend.viewmodel.ItemSetsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemSetCreateScreen(
    navController: NavHostController,
    shoppingListId: String,
    itemSetId: String,
    itemSetsViewModel: ItemSetsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val itemSets by itemSetsViewModel.itemSets.collectAsState()
    val itemSet = itemSets.find { it.id == itemSetId }
    val itemSetItems = itemSet?.itemList ?: emptyList()

    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(shoppingListId) {
        itemSetsViewModel.loadItemSets(shoppingListId, onSuccess = {})
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (itemSet != null) {
                            Text(
                                text = itemSet.name,
                                fontSize = 24.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                itemSetsViewModel.updateItemSet(
                                    shoppingListId,
                                    itemSet ?: ItemSet("", "", emptyList()),
                                    onSuccess = { itemSetName ->
                                        Toast.makeText(
                                            context,
                                            "$itemSetName saved",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_check_24),
                                contentDescription = "Save"
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            showDialog = true
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.DarkGray
                )
            )
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(itemSetItems, key = { it.tmpId }) { itemSetItem ->
                    ItemSetItemContainer(
                        itemSetId = itemSetId,
                        itemSetItem = itemSetItem,
                        onDelete = { deletedItem ->
                            itemSetsViewModel.deleteItemSetItem(itemSetId, deletedItem)
                        }
                    )
                }
                item {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        onClick = {
                            itemSetsViewModel.addEmptyItemSetItem(itemSetId)
                        }

                    ) {
                        Text("New Item")
                    }
                }
            }
        }
    )

    if (showDialog) {
        BasicAlertDialog(
            onDismissRequest = { showDialog = false },
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
                            text = "Save Item Set?",
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
                                    showDialog = false
                                    navController.popBackStack()
                                },
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = {
                                    itemSetsViewModel.updateItemSet(
                                        shoppingListId,
                                        itemSet ?: ItemSet("", "", emptyList()),
                                        onSuccess = { itemSetName ->
                                            Toast.makeText(
                                                context,
                                                "$itemSetName saved",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            showDialog = false
                                            navController.popBackStack()
                                        }
                                    )

                                },
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                Text("Save")
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun ItemSetItemContainer(
    itemSetId: String,
    itemSetItem: ItemSetItem,
    onDelete: (ItemSetItem) -> Unit,
    itemSetsViewModel: ItemSetsViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf(itemSetItem.name) }
    var amount by remember { mutableDoubleStateOf(itemSetItem.amount) }
    var amountText by remember { mutableStateOf(amount.toString().replace(".0", "")) }
    var unit by remember { mutableStateOf(itemSetItem.unit) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(0.5f)
                    .padding(end = 4.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { newName ->
                        name = newName
                        itemSetsViewModel.updateItemSetItem(
                            itemSetId,
                            itemSetItem.copy(name = newName)
                        )
                    },
                    placeholder = { Text("Item name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    )
                )
            }
            Column(
                modifier = Modifier
                    .weight(0.2f)
                    .padding(horizontal = 4.dp)
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { newValue ->
                        amountText = newValue.replace(",", ".")

                        if (amountText.isNotEmpty()) {
                            itemSetsViewModel.updateItemSetItem(
                                itemSetId,
                                itemSetItem.copy(amount = if (amountText == "0") 1.0 else amountText.toDouble())
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Column(
                modifier = Modifier
                    .weight(0.2f)
                    .padding(horizontal = 4.dp)
            ) {
                OutlinedTextField(
                    value = unit,
                    onValueChange = { newUnit ->
                        unit = newUnit
                        itemSetsViewModel.updateItemSetItem(
                            itemSetId,
                            itemSetItem.copy(unit = newUnit)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                    //TODO dropdown with kg,g,st,ml,l
                )
            }
            Column(
                modifier = Modifier.weight(0.1f)
            ) {
                IconButton(
                    onClick = {
                        onDelete(itemSetItem)
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
