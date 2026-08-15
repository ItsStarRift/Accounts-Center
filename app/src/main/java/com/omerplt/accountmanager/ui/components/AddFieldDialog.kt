package com.omerplt.accountmanager.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddFieldDialog(
    onDismiss: () -> Unit,
    onConfirm: (label: String, value: String, isCustom: Boolean) -> Unit
) {
    val predefinedLabels = listOf("Telefon Numarası", "Email", "Kullanıcı Adı", "Şifre", "Diğer/Özel Terim")
    var selectedLabel by remember { mutableStateOf(predefinedLabels.first()) }
    var customLabel by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var isCustomStep by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isCustomStep) "Özel Terim" else "Terim Ekle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!isCustomStep) {
                    predefinedLabels.forEach { label ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (label == selectedLabel),
                                onClick = { selectedLabel = label }
                            )
                            Text(text = label)
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = customLabel,
                        onValueChange = { customLabel = it },
                        label = { Text("Terim Adı (Örn: Google ile giriş)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text(if (isCustomStep) "Değer" else "$selectedLabel Değeri") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!isCustomStep && selectedLabel == "Diğer/Özel Terim") {
                        isCustomStep = true
                    } else {
                        val finalLabel = if (isCustomStep) customLabel else selectedLabel
                        if (finalLabel.isNotBlank() && value.isNotBlank()) {
                            onConfirm(finalLabel, value, isCustomStep)
                        }
                    }
                }
            ) {
                Text(if (!isCustomStep && selectedLabel == "Diğer/Özel Terim") "İleri" else "Ekle")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                if (isCustomStep) isCustomStep = false else onDismiss()
            }) {
                Text(if (isCustomStep) "Geri" else "İptal")
            }
        }
    )
}
