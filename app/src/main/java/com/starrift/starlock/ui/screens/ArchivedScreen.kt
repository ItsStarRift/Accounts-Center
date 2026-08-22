package com.starrift.starlock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.starrift.starlock.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedScreen(viewModel: ArchivedViewModel, onBackClick: () -> Unit) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val archivedApps by viewModel.archivedApps.collectAsState()
    val archivedAccounts by viewModel.archivedAccounts.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.archived)) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                }
            }
        )

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            SegmentedButton(
                selected = selectedTab == ArchivedTab.APPS,
                onClick = { viewModel.onTabChange(ArchivedTab.APPS) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
            ) { Text(stringResource(R.string.trash_tab_apps)) }
            SegmentedButton(
                selected = selectedTab == ArchivedTab.ACCOUNTS,
                onClick = { viewModel.onTabChange(ArchivedTab.ACCOUNTS) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
            ) { Text(stringResource(R.string.trash_tab_accounts)) }
            SegmentedButton(
                selected = selectedTab == ArchivedTab.FIELDS,
                onClick = { viewModel.onTabChange(ArchivedTab.FIELDS) },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
            ) { Text(stringResource(R.string.trash_tab_fields)) }
        }

        when (selectedTab) {
            ArchivedTab.FIELDS -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.archived_fields_placeholder),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
            else -> {
                val isEmpty = when (selectedTab) {
                    ArchivedTab.APPS -> archivedApps.isEmpty()
                    ArchivedTab.ACCOUNTS -> archivedAccounts.isEmpty()
                    else -> true
                }
                if (isEmpty) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            stringResource(R.string.archived_empty),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        when (selectedTab) {
                            ArchivedTab.APPS -> items(archivedApps, key = { it.id }) { app ->
                                ArchivedRow(
                                    title = app.name,
                                    subtitle = null,
                                    onUnarchive = { viewModel.unarchiveApp(app.id) }
                                )
                            }
                            ArchivedTab.ACCOUNTS -> items(archivedAccounts, key = { it.id }) { account ->
                                ArchivedRow(
                                    title = account.name,
                                    subtitle = account.appName,
                                    onUnarchive = { viewModel.unarchiveAccount(account.id) }
                                )
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchivedRow(
    title: String,
    subtitle: String?,
    onUnarchive: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onUnarchive) {
            Icon(Icons.Default.Unarchive, contentDescription = stringResource(R.string.unarchive))
        }
    }
    Spacer(modifier = Modifier.height(6.dp))
}
