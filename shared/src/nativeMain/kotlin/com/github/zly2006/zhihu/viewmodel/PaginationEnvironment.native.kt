/*
 * Zhihu++ iOS - Free & Ad-Free Zhihu client for iOS.
 * Copyright (C) 2024-2026
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation (version 3 only).
 */

package com.github.zly2006.zhihu.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.github.zly2006.zhihu.shared.account.IosAccountStore
import com.github.zly2006.zhihu.shared.data.ZhihuJson
import com.github.zly2006.zhihu.shared.data.installZhihuCommonClientConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Composable
actual fun rememberPaginationEnvironment(allowGuestAccess: Boolean): PaginationEnvironment {
    val accountStore = remember { IosAccountStore() }
    val session = remember { accountStore.load() }

    val client = remember(session) {
        HttpClient(Darwin) {
            installZhihuCommonClientConfig(
                cookies = session.cookies.toMutableMap(),
                userAgent = session.userAgent,
            )
        }
    }

    return remember(client, allowGuestAccess) {
        object : PaginationEnvironment {
            override fun httpClient(): HttpClient = client

            override suspend fun fetchJson(url: String, include: String): JsonObject? {
                return try {
                    val response = client.get(url) {
                        header("x-api-version", "3.0.91")
                    }
                    response.body<JsonObject>()
                } catch (e: Exception) {
                    println("[Zhihu++] fetchJson error: ${e.message}")
                    null
                }
            }

            override fun logDecodeFailure(tag: String?, item: JsonElement, error: Exception) {
                println("[Zhihu++] decode failure: ${error.message}")
            }

            override suspend fun handleFetchFailure(tag: String?, error: Exception) {
                println("[Zhihu++] fetch failure: ${error.message}")
            }

            override fun configureSignedRequest(builder: HttpRequestBuilder) {
                // TODO: Add zse96 v2 signing when Rust library is integrated
            }
        }
    }
}
