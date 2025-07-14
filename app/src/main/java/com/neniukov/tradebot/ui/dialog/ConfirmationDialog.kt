package com.neniukov.tradebot.ui.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp

@Composable
fun ConfirmationDialog(
    showDialog: Boolean,
    dialogData: DialogData,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = stringResource(id = dialogData.title),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 16.sp,
                )
            },
            text = {
                Text(
                    text = stringResource(id = dialogData.message),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(
                        text = stringResource(id = dialogData.confirmButtonText),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(id = dialogData.dismissButtonText),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        )
    }
}
