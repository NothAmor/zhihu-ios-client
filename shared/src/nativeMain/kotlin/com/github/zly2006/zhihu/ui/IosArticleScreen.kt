/*
 * Zhihu++ iOS - Free & Ad-Free Zhihu client for iOS.
 * Copyright (C) 2024-2026
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation (version 3 only).
 */

package com.github.zly2006.zhihu.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.github.zly2006.zhihu.navigation.Article
import com.github.zly2006.zhihu.shared.platform.rememberUserMessageSink
import com.github.zly2006.zhihu.viewmodel.ArticleViewModel

@Composable
fun rememberIosArticleScreen(
    article: Article,
    navEntry: NavBackStackEntry,
    navController: NavHostController,
) {
    val httpClient = rememberZhihuHttpClient()
    val userMessages = rememberUserMessageSink()

    val viewModel: ArticleViewModel = viewModel(navEntry) {
        ArticleViewModel(article, httpClient, userMessages) { onPause ->
            // iOS lifecycle observation - no direct equivalent of LifecycleObserver
            // For now, this is a no-op; pause callbacks can be handled by the Compose lifecycle
        }
    }

    ArticleScreen(article, viewModel)
}
