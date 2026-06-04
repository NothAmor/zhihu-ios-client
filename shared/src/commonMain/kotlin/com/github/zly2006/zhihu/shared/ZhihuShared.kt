/*
 * Zhihu++ iOS - Free & Ad-Free Zhihu client for iOS.
 * Copyright (C) 2024-2026
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation (version 3 only).
 */

package com.github.zly2006.zhihu.shared

import androidx.compose.runtime.Composable

internal expect val platformName: String

/**
 * Platform-specific main app composable.
 * Each platform provides its own implementation that wires up
 * [com.github.zly2006.zhihu.ui.ZhihuMain] with platform-specific
 * navigation, preferences, and article rendering.
 */
@Composable
expect fun ZhihuSharedApp()
