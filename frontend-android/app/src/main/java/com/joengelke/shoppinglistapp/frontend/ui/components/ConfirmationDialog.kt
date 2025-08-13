package com.joengelke.shoppinglistapp.frontend.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.joengelke.shoppinglistapp.frontend.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationDialog(
    text: String,
    acceptText: String,
    cancelText: String = stringResource(R.string.cancel),
    onDismiss: () -> Unit,
    onCancel: () -> Unit,
    onAccept: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = { onDismiss() },
        content = {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .padding(bottom = 8.dp),
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                onCancel()
                            },
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            Text(cancelText)
                        }
                        Button(
                            onClick = {
                                onAccept()
                            },
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            Text(text = acceptText)
                        }
                    }
                }
            }
        }
    )
}
