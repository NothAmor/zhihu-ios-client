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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.github.zly2006.zhihu.navigation.Account
import com.github.zly2006.zhihu.navigation.Daily
import com.github.zly2006.zhihu.navigation.Follow
import com.github.zly2006.zhihu.navigation.Home
import com.github.zly2006.zhihu.navigation.HotList
import com.github.zly2006.zhihu.navigation.NavDestination
import com.github.zly2006.zhihu.navigation.OnlineHistory
import com.github.zly2006.zhihu.navigation.TopLevelDestination
import com.github.zly2006.zhihu.shared.platform.rememberSettingsStore
import com.github.zly2006.zhihu.ui.subscreens.BOTTOM_BAR_ITEMS_PREFERENCE_KEY
import com.github.zly2006.zhihu.ui.subscreens.START_DESTINATION_PREFERENCE_KEY
import com.github.zly2006.zhihu.ui.subscreens.defaultBottomBarSelectionKeys
import com.github.zly2006.zhihu.ui.subscreens.navDestinationFromName
import com.github.zly2006.zhihu.ui.subscreens.normalizeBottomBarSelection
import com.github.zly2006.zhihu.ui.subscreens.resolveValidStartDestinationKey

@Composable
fun rememberIosZhihuMainNavigationState(
    onNavigate: (NavDestination) -> Unit,
): ZhihuMainNavigationState {
    var mainTabNavigationTarget by remember { mutableStateOf<TopLevelDestination?>(null) }
    var currentMainTabOpenFrom by remember { mutableStateOf<String?>(null) }

    return remember {
        ZhihuMainNavigationState(
            mainTabNavigationTarget = mainTabNavigationTarget,
            navigate = { destination ->
                if (destination is TopLevelDestination) {
                    mainTabNavigationTarget = destination
                }
                onNavigate(destination)
            },
            setCurrentMainTabOpenFrom = { openFrom ->
                currentMainTabOpenFrom = openFrom
            },
            consumeMainTabNavigationTarget = { destination ->
                if (mainTabNavigationTarget == destination) {
                    mainTabNavigationTarget = null
                }
            },
        )
    }
}

@Composable
fun rememberIosZhihuMainPreferenceState(): ZhihuMainPreferenceState {
    val settings = rememberSettingsStore()
    val allBottomBarItemKeys = remember {
        listOf(Home.name, Follow.name, HotList.name, Daily.name, OnlineHistory.name, Account.name)
    }
    return rememberZhihuMainPreferenceState {
        val duo3HomeAccount = settings.getBoolean("duo3_home_account", false)
        val selectedKeys = normalizeBottomBarSelection(
            settings.getStringSet(
                BOTTOM_BAR_ITEMS_PREFERENCE_KEY,
                defaultBottomBarSelectionKeys(duo3HomeAccount),
            ),
            duo3HomeAccount,
            enforceMinimumSelection = true,
        )
        ZhihuMainPreferenceSnapshot(
            duo3HomeAccount = duo3HomeAccount,
            duo3NavStyle = settings.getBoolean("duo3_nav_style", false),
            tapToScrollToTopEnabled = settings.getBoolean("bottomBarTapScrollToTop", true),
            autoHideBottomBar = settings.getBoolean("autoHideBottomBar", false),
            selectedBottomBarItemKeys = selectedKeys,
            startDestination = navDestinationFromName(
                resolveValidStartDestinationKey(
                    settings.getString(START_DESTINATION_PREFERENCE_KEY, Home.name),
                    allBottomBarItemKeys.filter { it in selectedKeys },
                ),
            ),
        )
    }
}
