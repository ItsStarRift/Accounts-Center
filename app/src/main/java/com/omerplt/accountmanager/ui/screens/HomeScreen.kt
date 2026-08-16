package com.omerplt.accountmanager.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.omerplt.accountmanager.R
import coil.compose.AsyncImage
import com.omerplt.accountmanager.data.AppCategory
import com.omerplt.accountmanager.data.AppWithAccountCount
import com.omerplt.accountmanager.ui.components.AddAppDialog
import com.omerplt.accountmanager.ui.theme.AccentOrange

private enum class BottomTab { UYGULAMALAR, AYARLAR }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAppClick: (Long) -> Unit,
    settingsContent: @Composable () -> Unit
) {
    val groups by viewModel.alphabeticalGroups.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(BottomTab.UYGULAMALAR) }

    Scaffold(
        floatingActionButton = {
            if (selectedTab == BottomTab.UYGULAMALAR && !isSearchActive) {
                val rotation by animateFloatAsState(
                    targetValue = if (showAddDialog) 45f else 0f,
                    animationSpec = tween(200),
                    label = "fab-rotation"
                )
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.cd_add_app),
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }
        },
        bottomBar = {
            FloatingBottomNav(
                selected = selectedTab,
                onSelect = { selectedTab = it }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                BottomTab.UYGULAMALAR -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        SearchBar(
                            query = query,
                            onQueryChange = viewModel::onSearchQueryChange,
                            onSearch = {},
                            active = isSearchActive,
                            onActiveChange = viewModel::onSearchActiveChange,
                            placeholder = { Text(stringResource(R.string.search_placeholder)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            SearchResultsList(
                                results = searchResults,
                                query = query,
                                onAppClick = {
                                    viewModel.onSearchActiveChange(false)
                                    onAppClick(it)
                                },
                                onDeleteApp = { viewModel.deleteApp(it) }
                            )
                        }

                        if (!isSearchActive) {
                            AppGroupedList(
                                groups = groups,
                                onAppClick = onAppClick,
                                onDeleteApp = { viewModel.deleteApp(it) }
                            )
                        }
                    }
                }
                BottomTab.AYARLAR -> {
                    settingsContent()
                }
            }
        }
    }

    if (showAddDialog) {
        AddAppDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, category, iconPath ->
                viewModel.addApp(name, category, iconPath)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun AppGroupedList(
    groups: List<Pair<Char, List<AppWithAccountCount>>>,
    onAppClick: (Long) -> Unit,
    onDeleteApp: (AppWithAccountCount) -> Unit
) {
    if (groups.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                stringResource(R.string.home_empty),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
    ) {
        groups.forEach { (letter, apps) ->
            item {
                Text(
                    text = letter.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp, top = 12.dp, bottom = 4.dp)
                )
            }
            items(apps, key = { it.id }) { app ->
                AppRow(
                    app = app,
                    onClick = { onAppClick(app.id) },
                    onDelete = { onDeleteApp(app) },
                    modifier = Modifier
                )
                Spacer(Modifier.height(8.dp))
            }
        }
        item { Spacer(Modifier.height(96.dp)) } // FAB'ın altında kalmasın diye
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchResultsList(
    results: List<AppWithAccountCount>,
    query: String,
    onAppClick: (Long) -> Unit,
    onDeleteApp: (AppWithAccountCount) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(results, key = { it.id }) { app ->
            HighlightedAppRow(
                app = app,
                query = query,
                onClick = { onAppClick(app.id) },
                onDelete = { onDeleteApp(app) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppRow(
    app: AppWithAccountCount,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showDeleteConfirm = true }
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIconCircle(app)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(app.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            Text(
                stringResource(R.string.account_count, app.accountCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_item_title, app.name)) },
            text = { Text(stringResource(R.string.delete_app_text)) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) { Text(stringResource(R.string.delete_confirm), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HighlightedAppRow(
    app: AppWithAccountCount,
    query: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showDeleteConfirm = true }
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIconCircle(app)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(text = highlightMatch(app.name, query), style = MaterialTheme.typography.bodyLarge)
            Text(
                stringResource(R.string.account_count, app.accountCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_item_title, app.name)) },
            text = { Text(stringResource(R.string.delete_app_text)) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) { Text(stringResource(R.string.delete_confirm), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

private fun highlightMatch(text: String, query: String): AnnotatedString {
    if (query.isBlank()) return AnnotatedString(text)
    val index = text.indexOf(query, ignoreCase = true)
    if (index < 0) return AnnotatedString(text)
    return buildAnnotatedString {
        append(text.substring(0, index))
        withStyle(SpanStyle(color = AccentOrange, fontWeight = FontWeight.Bold)) {
            append(text.substring(index, index + query.length))
        }
        append(text.substring(index + query.length))
    }
}

@Composable
private fun AppIconCircle(app: AppWithAccountCount) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        if (app.iconPath != null) {
            AsyncImage(
                model = app.iconPath,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(CircleShape)
            )
        } else {
            Text(
                app.name.take(1).uppercase(),
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FloatingBottomNav(
    selected: BottomTab,
    onSelect: (BottomTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 24.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            NavItem(
                icon = Icons.Default.Apps,
                label = stringResource(R.string.nav_apps),
                selected = selected == BottomTab.UYGULAMALAR,
                onClick = { onSelect(BottomTab.UYGULAMALAR) }
            )
            NavItem(
                icon = Icons.Default.Settings,
                label = stringResource(R.string.settings_title),
                selected = selected == BottomTab.AYARLAR,
                onClick = { onSelect(BottomTab.AYARLAR) }
            )
        }
    }
}

@Composable
private fun NavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(icon, contentDescription = label, tint = tint)
        Spacer(Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = tint)
    }
}
