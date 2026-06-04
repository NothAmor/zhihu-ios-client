/*
 * Zhihu++ iOS - Free & Ad-Free Zhihu client for iOS.
 * Copyright (C) 2024-2026
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation (version 3 only).
 */

package com.github.zly2006.zhihu.shared.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.github.zly2006.zhihu.ui.openIosUrl
import platform.UIKit.UIPasteboard
import platform.UIKit.UIScreen

@Composable
actual fun rememberExternalUrlOpener(): (String) -> Unit = remember { ::openIosUrl }

@Composable
actual fun rememberSystemUrlOpener(): (String) -> Unit = rememberExternalUrlOpener()

@Composable
actual fun rememberZhihuWebUrlOpener(): (String) -> Unit = rememberExternalUrlOpener()

@Composable
actual fun rememberImagePreviewOpener(): (String) -> Unit = rememberExternalUrlOpener()

@Composable
actual fun rememberPlainTextClipboard(): (label: String, text: String) -> Unit = remember {
    { _, text ->
        UIPasteboard.generalPasteboard.string = text
    }
}

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS has built-in swipe-to-go-back gesture via UINavigationController
    // Compose Multiplatform handles this automatically for pushed screens
}

@Composable
actual fun rememberSettingsStore(): SettingsStore = rememberIosSettingsStore()

@Composable
actual fun rememberScreenSizeDp(): ScreenSizeDp {
    val screen = remember { UIScreen.mainScreen }
    val bounds = remember { screen.bounds }
    val scale = remember { screen.scale }
    return remember(bounds, scale) {
        ScreenSizeDp(
            width = (bounds.useContents.size.width * scale).toFloat(),
            height = (bounds.useContents.size.height * scale).toFloat(),
        )
    }
}

@Composable
actual fun rememberUserMessageSink(): UserMessageSink {
    // iOS toast messages are handled by the Compose UI layer
    // The SnackbarHost in Material3 provides this functionality
    // For now, use a basic implementation; wire up to SwiftUI alert later
    return remember {
        UserMessageSink(
            showShortMessage = { message ->
                println("[Zhihu++] $message")
            },
            showLongMessage = { message ->
                println("[Zhihu++] $message")
            },
        )
    }
}

@Composable
actual fun rememberIsLiteVariant(): Boolean = false
