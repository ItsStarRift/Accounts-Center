package com.omerplt.accountmanager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Tam ekran dialogların içeriğini yumuşak bir "fade + scale" ile açar.
 * Modern Android 14 / M3 geçişlerine benzer bir his verir.
 */
@Composable
fun AnimatedDialogEntrance(content: @Composable () -> Unit) {
    val state = remember {
        MutableTransitionState(false).apply { targetState = true }
    }
    AnimatedVisibility(
        visibleState = state,
        enter = fadeIn(tween(220)) +
            scaleIn(initialScale = 0.94f, animationSpec = tween(220, easing = FastOutSlowInEasing)),
        exit = fadeOut(tween(140)) +
            scaleOut(targetScale = 0.96f, animationSpec = tween(140))
    ) {
        content()
    }
}
