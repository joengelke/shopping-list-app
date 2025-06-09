package com.joengelke.shoppinglistapp.frontend.ui.screens.itemsets

import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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

    val hasUnsavedChanges by itemSetsViewModel.hasUnsavedChanges.collectAsState()
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
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (itemSet != null) {
                            Text(
                                text = itemSet.name,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .weight(1f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (hasUnsavedChanges) {
                                showDialog = true
                            } else {
                                navController.popBackStack()
                            }
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    if (hasUnsavedChanges) {
                        IconButton(
                            onClick = {
                                itemSetsViewModel.updateItemSet(
                                    shoppingListId,
                                    itemSet ?: ItemSet("", "", emptyList()),
                                    onSuccess = { itemSetName ->
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.saved, itemSetName),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_save_24),
                                contentDescription = "Save",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
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
                            .padding(horizontal = 8.dp),
                        onClick = {
                            itemSetsViewModel.addEmptyItemSetItem(itemSetId)
                        }

                    ) {
                        Text(stringResource(R.string.new_item))
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
                            text = stringResource(R.string.save_item_set),
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
                                Text(stringResource(R.string.cancel))
                            }
                            Button(
                                onClick = {
                                    itemSetsViewModel.updateItemSet(
                                        shoppingListId,
                                        itemSet ?: ItemSet("", "", emptyList()),
                                        onSuccess = { itemSetName ->
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.saved, itemSetName),
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
                                Text(stringResource(R.string.save))
                            }
                        }
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

    var expandedUnitDropdown by remember { mutableStateOf(false) }
    val units = listOf("", "ml", "l", "g", "kg", stringResource(R.string.pieces_short))

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RectangleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.primary
        )
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
                    placeholder = { Text(stringResource(R.string.item_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
            Column(
                modifier = Modifier
                    .weight(0.18f)
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
            Column(
                modifier = Modifier
                    .weight(0.24f)
                    .padding(horizontal = 4.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expandedUnitDropdown,
                    onExpandedChange = { expandedUnitDropdown = !expandedUnitDropdown },
                ) {
                    OutlinedTextField(
                        value = unit.ifEmpty { " " },
                        onValueChange = { },
                        readOnly = true,
                        enabled = false,
                        singleLine = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnitDropdown)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, expandedUnitDropdown)
                            .clickable {
                                expandedUnitDropdown = true
                            },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = MaterialTheme.colorScheme.primary,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedUnitDropdown,
                        onDismissRequest = { expandedUnitDropdown = false}
                    ) {
                        units.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    unit = item
                                    itemSetsViewModel.updateItemSetItem(
                                        itemSetId,
                                        itemSetItem.copy(unit = item)
                                    )
                                    expandedUnitDropdown = false
                                }
                            )
                        }
                    }
                }
            }
            Column(
                modifier = Modifier.weight(0.08f)
            ) {
                IconButton(
                    onClick = {
                        onDelete(itemSetItem)
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_delete_24),
                        contentDescription = "Remove"
                    )
                }
            }
        }
    }
    HorizontalDivider(
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    )
}
