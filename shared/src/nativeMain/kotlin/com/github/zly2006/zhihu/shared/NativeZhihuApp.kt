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
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.github.zly2006.zhihu.theme.ThemeManager
import com.github.zly2006.zhihu.theme.ZhihuTheme
import com.github.zly2006.zhihu.ui.ZhihuMain
import com.github.zly2006.zhihu.ui.ZhihuMainPlatformAdapter
import com.github.zly2006.zhihu.ui.rememberIosArticleScreen
import com.github.zly2006.zhihu.ui.rememberIosZhihuMainNavigationState
import com.github.zly2006.zhihu.ui.rememberIosZhihuMainPreferenceState

@Composable
actual fun ZhihuSharedApp() {
    val navController = rememberNavController()
    val navigationState = rememberIosZhihuMainNavigationState(
        onNavigate = { destination ->
            navController.navigate(destination) {
                launchSingleTop = true
            }
        },
    )
    val preferenceState = rememberIosZhihuMainPreferenceState()

    val platformAdapter = remember {
        ZhihuMainPlatformAdapter(
            article = { article, navEntry ->
                rememberIosArticleScreen(article, navEntry, navController)
            },
        )
    }

    ZhihuTheme {
        ZhihuMain(
            navController = navController,
            navigationState = navigationState,
            preferenceState = preferenceState,
            isDarkTheme = ThemeManager.isDarkTheme,
            platformAdapter = platformAdapter,
        )
    }
}
