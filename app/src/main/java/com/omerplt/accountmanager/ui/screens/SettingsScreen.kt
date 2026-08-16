package com.omerplt.accountmanager.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private const val GITHUB_URL = "https://github.com/ItsStarRift/Accounts-Center"
private const val FEEDBACK_EMAIL = "omerplt.dev@gmail.com"
private const val APP_VERSION = "0.1-alpha"

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showAboutDialog by remember { mutableStateOf(false) }
    var showImportConfirmDialog by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val success = viewModel.exportTo(context, uri)
                Toast.makeText(
                    context,
                    if (success) "Veriler dışa aktarıldı" else "Dışa aktarma başarısız oldu",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingImportUri = uri
            showImportConfirmDialog = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            "Ayarlar",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(20.dp))

        SettingsGroup {
            SettingsRow(
                icon = Icons.Default.Lock,
                title = "Uygulama Kilidi",
                subtitle = "Yakında eklenecek",
                onClick = {
                    Toast.makeText(context, "Uygulama kilidi yakında eklenecek", Toast.LENGTH_SHORT).show()
                }
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Default.Language,
                title = "Dil",
                subtitle = "Sistem varsayılanı (Türkçe)",
                onClick = {
                    Toast.makeText(context, "Şu an sadece Türkçe destekleniyor", Toast.LENGTH_SHORT).show()
                }
            )
        }

        Spacer(Modifier.height(20.dp))

        SettingsGroup {
            SettingsRow(
                icon = Icons.Default.Upload,
                title = "Verileri dışa aktar",
                subtitle = "Tüm verileri .json dosyası olarak indir",
                onClick = { exportLauncher.launch("hesap-yoneticisi-yedek.json") }
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Default.Download,
                title = "Verileri içe aktar",
                subtitle = "Bir .json yedeğinden geri yükle",
                onClick = { importLauncher.launch(arrayOf("application/json")) }
            )
        }

        Spacer(Modifier.height(20.dp))

        SettingsGroup {
            SettingsRow(
                icon = Icons.Default.Email,
                title = "Geri Bildirim Gönder",
                subtitle = FEEDBACK_EMAIL,
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:$FEEDBACK_EMAIL")
                        putExtra(Intent.EXTRA_SUBJECT, "Hesap Yöneticisi - Geri Bildirim")
                    }
                    context.startActivity(Intent.createChooser(intent, "Geri bildirim gönder"))
                }
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Default.Code,
                title = "Github Sayfası",
                subtitle = "Kaynak kodu görüntüle",
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL)))
                }
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Default.Info,
                title = "Uygulama Hakkında",
                subtitle = "Sürüm $APP_VERSION",
                onClick = { showAboutDialog = true }
            )
        }

        Spacer(Modifier.height(96.dp))
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("Hesap Yöneticisi") },
            text = {
                Text(
                    "Sürüm $APP_VERSION\n\n" +
                        "Tüm verileriniz cihazınızda şifreli olarak saklanır, " +
                        "hiçbir veri sunucuya gönderilmez."
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("Kapat") }
            }
        )
    }

    if (showImportConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showImportConfirmDialog = false },
            title = { Text("Verileri içe aktar") },
            text = {
                Text(
                    "Bu işlem, cihazınızdaki TÜM mevcut verilerin yerine seçtiğiniz " +
                        "yedeği koyacak. Şu anki veriler silinecek ve geri alınamayacak. Devam edilsin mi?"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val uri = pendingImportUri
                    showImportConfirmDialog = false
                    if (uri != null) {
                        scope.launch {
                            val success = viewModel.importFrom(context, uri)
                            Toast.makeText(
                                context,
                                if (success) "Veriler içe aktarıldı" else "İçe aktarma başarısız oldu",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }) { Text("Evet, değiştir") }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirmDialog = false }) { Text("İptal") }
            }
        )
    }
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        content()
    }
}

@Composable
private fun SettingsDivider() {
    Divider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}
