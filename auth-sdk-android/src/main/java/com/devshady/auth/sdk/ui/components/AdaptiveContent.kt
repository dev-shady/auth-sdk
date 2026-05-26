package com.devshady.auth.sdk.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowWidthSizeClass

@Composable
fun AdaptiveContent(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    // 1. Grab the window width sizing class from the official M3 Adaptive API
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val widthClass = adaptiveInfo.windowSizeClass.windowWidthSizeClass

    // 2. Google Way: Scale up progressively. Content NEVER shrinks as screens grow.
    val maxWidth = when (widthClass) {
        WindowWidthSizeClass.COMPACT -> 440.dp    // Small phones: standard content width limits
        WindowWidthSizeClass.MEDIUM -> 600.dp     // Foldables/Small tablets: expands out naturally
        WindowWidthSizeClass.EXPANDED -> 840.dp   // Desktops/Large Tablets: gives full canvas room
        else -> 840.dp
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                // Fluid up to the calculated max breakpoint safely
                .widthIn(max = maxWidth)
                .fillMaxWidth()
                // Google Layout Grid recommends 16dp on mobile, 24dp+ on larger display views
                .padding(if (widthClass == WindowWidthSizeClass.COMPACT) 16.dp else 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            content = content
        )
    }
}

