/*
 * Zhihu++ iOS - Free & Ad-Free Zhihu client for iOS.
 * Copyright (C) 2024-2026
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation (version 3 only).
 */

package com.github.zly2006.zhihu.viewmodel.local

import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

/**
 * Manual actual implementation for iOS since KSP Room compiler
 * is not available for Kotlin/Native targets.
 *
 * Local content database works with in-memory stubs on iOS.
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
actual object LocalContentDatabaseConstructor : RoomDatabaseConstructor<LocalContentDatabase> {
    override fun initialize(): LocalContentDatabase {
        throw NotImplementedError("Room database is not available on iOS. Use in-memory local content implementations.")
    }
}
