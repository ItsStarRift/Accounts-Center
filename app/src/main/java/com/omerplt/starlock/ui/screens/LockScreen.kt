package com.omerplt.starlock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omerplt.starlock.util.PinManager
import kotlinx.coroutines.delay

@Composable
fun LockScreen(
    pinManager: PinManager,
    onUnlocked: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var remainingLockout by remember { mutableStateOf(pinManager.getRemainingLockoutSeconds()) }

    LaunchedEffect(remainingLockout) {
        if (remainingLockout > 0) {
            delay(1000)
            remainingLockout = pinManager.getRemainingLockoutSeconds()
        }
    }

    LaunchedEffect(pin) {
        if (pin.length == 4) {
            if (pinManager.verifyPin(pin)) {
                onUnlocked()
            } else {
                isError = true
                pin = ""
                remainingLockout = pinManager.getRemainingLockoutSeconds()
                delay(400)
                isError = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Hoş Geldiniz",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (remainingLockout > 0) "Çok fazla hatalı deneme. ${remainingLockout}s bekleyin." 
                   else if (isError) "Hatalı PIN, tekrar deneyin." 
                   else "Devam etmek için PIN kodunuzu girin",
            color = if (isError || remainingLockout > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        // PIN Noktaları
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 4) {
                val isFilled = i < pin.length
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (isFilled) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(64.dp))

        // NumPad Tuş Takımı
        val buttons = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "DEL")
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { btn ->
                    if (btn.isEmpty()) {
                        Spacer(modifier = Modifier.size(72.dp))
                    } else {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .clickable(
                                    enabled = remainingLockout == 0L,
                                    onClick = {
                                        if (btn == "DEL") {
                                            if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                        } else {
                                            if (pin.length < 4) pin += btn
                                        }
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (btn == "DEL") {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Sil",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            } else {
                                Text(
                                    text = btn,
                                    fontSize = 28.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
