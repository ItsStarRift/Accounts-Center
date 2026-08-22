package com.starrift.starlock.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ArchivedScreen(viewModel: ArchivedViewModel, onBackClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Archived test")
    }
}
