package com.joengelke.shoppinglistapp.frontend.ui.screens.itemsets

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.joengelke.shoppinglistapp.frontend.R
import com.joengelke.shoppinglistapp.frontend.models.ItemSet
import com.joengelke.shoppinglistapp.frontend.navigation.Routes
import com.joengelke.shoppinglistapp.frontend.utils.FileUtils.openFile
import com.joengelke.shoppinglistapp.frontend.utils.JsonHelper.json
import com.joengelke.shoppinglistapp.frontend.viewmodel.ItemSetsViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemSetOverviewScreen(
    navController: NavHostController,
    shoppingListId: String,
    itemSetsViewModel: ItemSetsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
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

    // upload itemSet
    /*
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val jsonString = inputStream.bufferedReader().readText()
                    itemSetsViewModel.uploadItemSet(shoppingListId, json.decodeFromString<ItemSet>(jsonString))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
     */

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
                            text = stringResource(R.string.item_sets),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .weight(1f)
                        )
                    }
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
                actions = {
                    /*
                    IconButton(onClick = {
                        launcher.launch("application/json")
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_upload_24),
                            contentDescription = "Upload JSON file",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                     */
                    // dropdown menu possible
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
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
                    modifier = Modifier
                        .fillMaxSize()
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
                            onShowReceipt = {itemSetId ->
                                itemSetsViewModel.getReceiptFile(
                                    itemSetId,
                                    onSuccess = { file ->
                                        openFile(context, file)
                                    }
                                )
                            },
                            //onDownload = { downloadItemSet -> downloadItemSet(context, downloadItemSet) },
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
                            },
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 6.dp
                            ),
                        ) {
                            Text(stringResource(R.string.new_item_set))
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
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
                        placeholder = { Text(stringResource(R.string.item_set_name)) },
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
                        Text(
                            stringResource(R.string.cancel),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (itemSetName.isNotBlank()) {
                                itemSetsViewModel.createEmptyItemSet(
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
                        Text(
                            stringResource(R.string.create),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        )
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
    onShowReceipt: (String) -> Unit,
    //onDownload: (ItemSet) -> Unit,
    onEdit: (ItemSet) -> Unit,
    onDelete: (String) -> Unit
) {
    var delete by remember { mutableStateOf(false) }
    var edit by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(itemSet.name) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, top = 8.dp)
            .clickable {
                onEditItems(itemSet.id)
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            when {
                edit -> {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                        },
                        modifier = Modifier
                            .padding(start = 12.dp, top = 4.dp, bottom = 4.dp, end = 8.dp)
                            .weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary
                        )
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
                            contentDescription = "save item set name"
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
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                delete -> {
                    Text(
                        text = stringResource(R.string.are_you_sure),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(20.dp)
                            .weight(1f),
                    )

                    IconButton(
                        onClick = {
                            delete = false
                            onDelete(itemSet.id)
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_check_24),
                            contentDescription = "confirm delete"
                        )
                    }
                    IconButton(
                        onClick = {
                            delete = false
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_close_24),
                            contentDescription = "decline delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                else -> {
                    Text(
                        text = itemSet.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(20.dp)
                            .weight(1f)
                    )
                    if (itemSet.receiptFileId.isNotBlank()) {
                        IconButton(
                            onClick = {
                                onShowReceipt(itemSet.id)
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_insert_photo_24),
                                contentDescription = "show receipt file"
                            )
                        }
                    }
                    /*
                    IconButton(
                        onClick = {
                            onDownload(itemSet)
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_download_24),
                            contentDescription = "download item set"
                        )
                    }
                     */
                    IconButton(
                        onClick = {
                            edit = true
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_edit_24),
                            contentDescription = "edit item set name"
                        )
                    }
                    IconButton(
                        onClick = {
                            delete = true
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_delete_24),
                            contentDescription = "delete item set"
                        )
                    }
                }
            }
        }
    }
}

fun downloadItemSet(context: Context, itemSet: ItemSet) {
    try {
        // Create Downloads folder path
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) downloadsDir.mkdirs()

        // Define file
        val safeName = itemSet.name.replace(Regex("[^a-zA-Z0-9-_]"), "_") // sanitize file name
        val fileName = "ItemSet_$safeName.json"
        val file = File(downloadsDir, fileName)

        // Write file
        file.writeText(json.encodeToString(itemSet))

        // Notify media scanner so it shows up in file explorers
        MediaScannerConnection.scanFile(
            context,
            arrayOf(file.absolutePath),
            null,
            null
        )

        Toast.makeText(context, "ItemSet downloaded to Downloads", Toast.LENGTH_SHORT).show()

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Download failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}
