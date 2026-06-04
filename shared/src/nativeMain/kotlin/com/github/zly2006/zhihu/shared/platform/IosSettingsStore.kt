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
import platform.Foundation.NSNumber
import platform.Foundation.NSUserDefaults

@Composable
fun rememberIosSettingsStore(): SettingsStore {
    val defaults = remember { NSUserDefaults.standardUserDefaults }

    return remember(defaults) {
        SettingsStore(
            getBoolean = { key, defaultValue ->
                if (defaults.objectForKey(key) != null) defaults.boolForKey(key) else defaultValue
            },
            putBoolean = { key, value ->
                defaults.setBool(value, key)
                defaults.synchronize()
            },
            getString = { key, defaultValue ->
                defaults.stringForKey(key) ?: defaultValue
            },
            putString = { key, value ->
                defaults.setObject(value, key)
                defaults.synchronize()
            },
            getStringOrNull = { key ->
                defaults.stringForKey(key)
            },
            putStringSet = { key, value ->
                defaults.setObject(value.toList(), key)
                defaults.synchronize()
            },
            getStringSet = { key, defaultValue ->
                (defaults.arrayForKey(key) as? List<*>)?.filterIsInstance<String>()?.toSet() ?: defaultValue
            },
            getInt = { key, defaultValue ->
                if (defaults.objectForKey(key) != null) defaults.integerForKey(key).toInt() else defaultValue
            },
            putInt = { key, value ->
                defaults.setInteger(value.toLong(), key)
                defaults.synchronize()
            },
            getLong = { key, defaultValue ->
                if (defaults.objectForKey(key) != null) {
                    // NSUserDefaults stores numbers; try to extract as integer
                    defaults.integerForKey(key)
                } else {
                    defaultValue
                }
            },
            putLong = { key, value ->
                defaults.setObject(NSNumber(long = value), key)
                defaults.synchronize()
            },
            getFloat = { key, defaultValue ->
                if (defaults.objectForKey(key) != null) defaults.floatForKey(key) else defaultValue
            },
            putFloat = { key, value ->
                defaults.setFloat(value, key)
                defaults.synchronize()
            },
            remove = { key ->
                defaults.removeObjectForKey(key)
                defaults.synchronize()
            },
        )
    }
}
