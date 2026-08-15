package com.omerplt.accountmanager.ui.components

import android.Manifest
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.omerplt.accountmanager.data.AppCategory
import com.omerplt.accountmanager.ui.components.AnimatedDialogEntrance
import com.omerplt.accountmanager.util.CameraFileHelper
import com.omerplt.accountmanager.util.IconFetcher
import kotlinx.coroutines.launch

@Composable
fun AddAppDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, category: AppCategory, iconPath: String?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<AppCategory?>(null) }
    var showCategoryError by remember { mutableStateOf(false) }
    var showNameWarning by remember { mutableStateOf(false) }

    var iconPath by remember { mutableStateOf<String?>(null) }
    var isFetchingIcon by remember { mutableStateOf(false) }
    var fetchedPreview by remember { mutableStateOf<Bitmap?>(null) }
    var showPickerSheet by remember { mutableStateOf(false) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            CameraFileHelper.persistImageFromUri(context, it)?.let { path -> iconPath = path }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraUri?.let {
                CameraFileHelper.persistImageFromUri(context, it)?.let { path -> iconPath = path }
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = CameraFileHelper.createTempImageUri(context)
            cameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    fun startIconSearch() {
        if (name.isBlank()) {
            showNameWarning = true
            return
        }
        showNameWarning = false
        scope.launch {
            if (IconFetcher.isOnline(context)) {
                isFetchingIcon = true
                val bitmap = IconFetcher.tryFetchPreview(name)
                isFetchingIcon = false
                if (bitmap != null) {
                    fetchedPreview = bitmap
                } else {
                    showPickerSheet = true
                }
            } else {
                showPickerSheet = true
            }
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        AnimatedDialogEntrance {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Üst bar: X  Liste Oluştur ............ Kaydet
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat")
                    }
                    Text(
                        text = "Liste Oluştur",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f).padding(start = 4.dp)
                    )
                    Button(
                        onClick = {
                            if (selectedCategory == null) {
                                showCategoryError = true
                            } else {
                                onSave(name, selectedCategory!!, iconPath)
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        enabled = name.isNotBlank()
                    ) {
                        Text("Kaydet")
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(24.dp))

                    // İkon dairesi + küçük (+) rozeti
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable { startIconSearch() },
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                isFetchingIcon -> CircularProgressIndicator(modifier = Modifier.size(28.dp))
                                iconPath != null -> AsyncImage(
                                    model = iconPath,
                                    contentDescription = "Seçilen ikon",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                                )
                                else -> Icon(
                                    imageVector = Icons.Default.QuestionMark,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .clickable { startIconSearch() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "İkon ekle",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (showNameWarning) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Önce uygulamanın adını girin",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Ad") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(20.dp))

                    // Uygulama / Oyun segmentli seçim
                    SingleChoiceSegment(
                        selected = selectedCategory,
                        onSelect = {
                            selectedCategory = it
                            showCategoryError = false
                        }
                    )

                    if (showCategoryError) {
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Lütfen ikisinden birini seçiniz",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
        }
    }

    // Otomatik bulunan ikonu onaylatma penceresi
    fetchedPreview?.let { bitmap ->
        AlertDialog(
            onDismissRequest = { fetchedPreview = null },
            title = { Text("Uygulamanızın ikonu bu mu?") },
            text = {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(96.dp).clip(CircleShape)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        iconPath = IconFetcher.saveBitmapToInternalStorage(context, bitmap)
                        fetchedPreview = null
                    }
                }) { Text("Evet") }
            },
            dismissButton = {
                TextButton(onClick = {
                    fetchedPreview = null
                    showPickerSheet = true
                }) { Text("Hayır") }
            }
        )
    }

    // Galeri / Kamera seçim penceresi
    if (showPickerSheet) {
        AlertDialog(
            onDismissRequest = { showPickerSheet = false },
            title = { Text("İkon seç") },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showPickerSheet = false
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Text("Kamera")
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showPickerSheet = false
                                galleryLauncher.launch("image/*")
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Text("Galeri")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPickerSheet = false }) { Text("İptal") }
            }
        )
    }
}

@Composable
private fun SingleChoiceSegment(
    selected: AppCategory?,
    onSelect: (AppCategory) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        SegmentOption(
            text = "Uygulama",
            selected = selected == AppCategory.UYGULAMA,
            modifier = Modifier.weight(1f)
        ) { onSelect(AppCategory.UYGULAMA) }
        SegmentOption(
            text = "Oyun",
            selected = selected == AppCategory.OYUN,
            modifier = Modifier.weight(1f)
        ) { onSelect(AppCategory.OYUN) }
    }
}

@Composable
private fun SegmentOption(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
        }
        Text(text, color = fg, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}
