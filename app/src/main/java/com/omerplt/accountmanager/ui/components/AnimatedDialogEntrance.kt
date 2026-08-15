package com.omerplt.accountmanager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * Liste öğelerine (uygulama/hesap satırları) hafif bir "aşağıdan yukarı + fade" giriş
 * animasyonu uygular. LazyColumn'un animateItem() gibi daha yeni Compose sürümlerine
 * özgü API'lerine bağımlı olmadığı için her Compose sürümüyle güvenle çalışır.
 */
@Composable
fun AnimatedListItem(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val state = remember {
        MutableTransitionState(false).apply { targetState = true }
    }
    AnimatedVisibility(
        visibleState = state,
        enter = fadeIn(tween(280)) + slideInVertically(
            initialOffsetY = { it / 6 },
            animationSpec = tween(280)
        ),
        modifier = modifier
    ) {
        content()
    }
}

