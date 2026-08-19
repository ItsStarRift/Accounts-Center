package com.omerplt.accountmanager.ui.components

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import androidx.compose.ui.res.stringResource
import com.omerplt.accountmanager.R
import com.omerplt.accountmanager.data.AppCategory
import com.omerplt.accountmanager.util.CameraFileHelper

@Composable
fun AddAppDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, category: AppCategory, iconPath: String?) -> Unit,
    existingName: String? = null,
    existingCategory: AppCategory? = null,
    existingIconPath: String? = null
) {
    val context = LocalContext.current
    val isEditMode = existingName != null

    var name by remember { mutableStateOf(existingName ?: "") }
    var selectedCategory by remember { mutableStateOf(existingCategory) }
    var showCategoryError by remember { mutableStateOf(false) }
    var showNameWarning by remember { mutableStateOf(false) }

    var iconPath by remember { mutableStateOf(existingIconPath) }
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

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        AnimatedDialogEntrance {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(modifier = Modifier.fillMaxSize()) {

                    // Üst bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
                        }
                        Text(
                            text = if (isEditMode) stringResource(R.string.edit_app_title) else stringResource(R.string.create_list_title),
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
                            Text(stringResource(R.string.save_btn))
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(24.dp))

                        // İkon dairesi + küçük (+) rozeti (Tek Tıklama Alanı)
                        Box(
                            contentAlignment = Alignment.BottomEnd,
                            modifier = Modifier.clickable {
                                if (name.isBlank()) {
                                    showNameWarning = true
                                } else {
                                    showNameWarning = false
                                    showPickerSheet = true
                                }
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                if (iconPath != null) {
                                    AsyncImage(
                                        model = iconPath,
                                        contentDescription = stringResource(R.string.cd_selected_icon),
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
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
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = stringResource(R.string.cd_add_icon),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        if (showNameWarning) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                stringResource(R.string.enter_name_first),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(Modifier.height(24.dp))

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(stringResource(R.string.dialog_name_label)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(20.dp))

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
                                    stringResource(R.string.please_select_category),
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

    if (showPickerSheet) {
        AlertDialog(
            onDismissRequest = { showPickerSheet = false },
            title = { Text(stringResource(R.string.select_icon_title)) },
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
                        Text(stringResource(R.string.camera))
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
                        Text(stringResource(R.string.gallery))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPickerSheet = false }) { Text(stringResource(R.string.cancel)) }
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
            text = stringResource(R.string.category_app),
            selected = selected == AppCategory.UYGULAMA,
            modifier = Modifier.weight(1f)
        ) { onSelect(AppCategory.UYGULAMA) }
        SegmentOption(
            text = stringResource(R.string.category_game),
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
