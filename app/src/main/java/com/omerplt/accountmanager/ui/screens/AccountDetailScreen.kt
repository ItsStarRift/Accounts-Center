package com.omerplt.accountmanager.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.omerplt.accountmanager.data.AccountField
import com.omerplt.accountmanager.ui.components.AddFieldDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    viewModel: AccountDetailViewModel,
    onBackClick: () -> Unit
) {
    val fields by viewModel.fields.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hesap Detayları") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Terim Ekle")
            }
        }
    ) { padding ->
        if (fields.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Henüz terim eklenmemiş.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                contentPadding = padding,
                modifier = Modifier.fillMaxSize()
            ) {
                items(fields) { field ->
                    FieldItemCard(field = field)
                }
            }
        }

        if (showAddDialog) {
            AddFieldDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { label, value, isCustom ->
                    viewModel.addField(label, value, isCustom)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun FieldItemCard(field: AccountField) {
    val context = LocalContext.current
    var isPasswordVisible by remember { mutableStateOf(false) }
    val isPassword = field.label.contains("Şifre", ignoreCase = true) || field.label.contains("Password", ignoreCase = true)

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = field.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                if (isPassword && !isPasswordVisible) {
                    Text(text = "••••••••", style = MaterialTheme.typography.bodyLarge)
                } else {
                    Text(text = field.value, style = MaterialTheme.typography.bodyLarge)
                }
            }
            if (isPassword) {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Görünürlük"
                    )
                }
            }
            IconButton(onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(field.label, field.value)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Kopyalandı", Toast.LENGTH_SHORT).show()
            }) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Kopyala")
            }
        }
    }
}
