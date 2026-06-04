/*
 * Zhihu++ iOS - Free & Ad-Free Zhihu client for iOS.
 * Copyright (C) 2024-2026
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation (version 3 only).
 */

package com.github.zly2006.zhihu.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.github.zly2006.zhihu.navigation.Article
import com.github.zly2006.zhihu.shared.account.IosAccountStore
import com.github.zly2006.zhihu.shared.platformName
import com.github.zly2006.zhihu.shared.data.RecommendationMode
import com.github.zly2006.zhihu.shared.data.ZhihuJson
import com.github.zly2006.zhihu.shared.data.fetchVerifiedZhihuProfile
import com.github.zly2006.zhihu.shared.data.installZhihuCommonClientConfig
import com.github.zly2006.zhihu.shared.notification.NotificationSettingsStore
import com.github.zly2006.zhihu.shared.platform.UserMessageSink
import com.github.zly2006.zhihu.shared.platform.rememberUserMessageSink
import com.github.zly2006.zhihu.ui.components.rememberShareDialogRuntime
import com.github.zly2006.zhihu.viewmodel.NotificationPaginationEnvironment
import com.github.zly2006.zhihu.viewmodel.NotificationViewModel
import com.github.zly2006.zhihu.viewmodel.feed.HomeFeedViewModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

@Composable
actual fun rememberArticleActionsRuntime(): ArticleActionsRuntime {
    val userMessages = rememberUserMessageSink()
    val dialogShareRuntime = rememberShareDialogRuntime()
    return remember(userMessages, dialogShareRuntime) {
        object : ArticleActionsRuntime {
            override var ttsState: TtsState by mutableStateOf(TtsState.Ready)
                private set
            override val shareRuntime = dialogShareRuntime

            override fun toggleSpeech(title: String, content: String) =
                userMessages.showMessage("iOS TTS 暂未实现")

            override fun openArticleInBrowser(article: Article) = openIosUrl(articleWebUrl(article))
        }
    }
}

@Composable
actual fun rememberNotificationScreenRuntime(
    viewModel: NotificationViewModel,
    settingsStore: NotificationSettingsStore,
): NotificationScreenRuntime {
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

    return remember(client, settingsStore) {
        NotificationScreenRuntime(
            environment = object : NotificationPaginationEnvironment {
                override val notificationSettingsStore = settingsStore

                override fun httpClient(): HttpClient = client

                override suspend fun fetchJson(url: String, include: String): JsonObject {
                    return client.get(url) {
                        header("x-api-version", "3.0.91")
                    }.body()
                }

                override fun logDecodeFailure(tag: String?, item: JsonElement, error: Exception) {
                    println("[Zhihu++] Notification decode failure: ${error.message}")
                }

                override suspend fun handleFetchFailure(tag: String?, error: Exception) {
                    println("[Zhihu++] Notification fetch failure: ${error.message}")
                }

                override fun configureSignedRequest(builder: HttpRequestBuilder) {
                }
            },
            showDebugCopy = false,
        )
    }
}

@Composable
actual fun rememberArticleScreenRuntime(): ArticleScreenRuntime = remember {
    object : ArticleScreenRuntime {
        override val articleHost: ArticleHost? = null

        override val previewPreloader = ArticlePreviewPreloader { _, _, _, _ -> }
    }
}

@Composable
actual fun ArticleWebViewContent(
    article: Article,
    html: String,
    title: String,
    scrollState: ScrollState,
    rememberedScrollY: Int,
    rememberedScrollYSync: Boolean,
    onRememberedScrollYSyncChange: (Boolean) -> Unit,
    onImageLoadFailed: () -> Unit,
    onDoubleTap: () -> Unit,
) {
    // iOS WebView not yet integrated - fallback to Markdown rendering
    // The ArticleScreen will use Markdown rendering when WebView is unavailable
}

actual fun Modifier.articleMarkdownSelectionWorkaround(): Modifier = this

@Composable
actual fun rememberCommentScreenRuntime(): CommentScreenRuntime {
    val userMessages = rememberUserMessageSink()
    return remember(userMessages) {
        object : CommentScreenRuntime {
            override fun saveImage(imageUrl: String) =
                userMessages.showMessage("图片保存暂未实现")

            override fun shareImage(imageUrl: String) =
                userMessages.showMessage("图片分享暂未实现")
        }
    }
}

@Composable
actual fun rememberCommentEmojiInlineContent(emojiKeys: Set<String>): Map<String, InlineTextContent> = emptyMap()

actual fun commentEmojiInlineKey(placeholder: String): String? = null

actual fun Modifier.commentSelectionWorkaround(): Modifier = this

@Composable
actual fun rememberHomeScreenRuntime(recommendationMode: RecommendationMode): HomeScreenRuntime {
    val userMessages = rememberUserMessageSink()
    val accountStore = remember { IosAccountStore() }
    val session = remember { accountStore.load() }
    val isLoggedIn = session.login && session.username.isNotEmpty()
    val avatarUrl = session.profile?.avatarUrl

    val viewModel = remember { HomeFeedViewModel() }

    return remember(userMessages, viewModel, isLoggedIn, avatarUrl) {
        HomeScreenRuntime(
            account = HomeAccountState(
                isLoggedIn = isLoggedIn,
                avatarUrl = avatarUrl,
            ),
            updateAnnouncement = null,
            installedAtLeastThreeHours = false,
            isDebuggable = false,
            viewModel = viewModel,
            requestLogin = { userMessages.showMessage("请先登录知乎账号") },
            recordLocalItemOpened = { },
            recordLocalItemFeedback = { _, _ -> false },
        )
    }
}

@Composable
actual fun rememberAccountSettingsPlatformRuntime(): AccountSettingsRuntime {
    val userMessages = rememberUserMessageSink()
    val accountStore = remember { IosAccountStore() }
    val session = remember { accountStore.load() }

    val accountState = remember(session) {
        mutableStateOf(
            AccountSettingsAccountState(
                login = session.login,
                username = session.username,
                avatarUrl = session.profile?.avatarUrl,
                id = session.profile?.id ?: "",
                urlToken = session.profile?.urlToken,
            )
        )
    }

    return remember(userMessages, accountStore, accountState) {
        AccountSettingsRuntime(
            accountState = accountState,
            refreshProfile = {
                if (session.login) {
                    val client = accountStore.createHttpClient(session.cookies.toMutableMap())
                    try {
                        val profile = fetchVerifiedZhihuProfile(client)
                        if (profile != null) {
                            val updated = accountStore.load()
                            accountStore.save(
                                updated.copy(
                                    profile = com.github.zly2006.zhihu.shared.account.ZhihuAccountProfileSnapshot(
                                        id = profile.id,
                                        name = profile.name,
                                        urlToken = profile.urlToken,
                                        userType = profile.userType,
                                        avatarUrl = profile.avatarUrl,
                                    )
                                )
                            )
                        }
                    } catch (_: Exception) {
                    }
                }
            },
            requestLogin = { userMessages.showMessage("请使用网页版登录功能") },
            requestQrLoginScan = { userMessages.showMessage("扫码登录暂未实现") },
            logout = {
                accountStore.clear()
                accountState.value = AccountSettingsAccountState()
            },
            appVersionInfo = { "iOS ${platformName}" },
            selectMainTab = { },
        )
    }
}

@Composable
actual fun rememberPinScreenRuntime(): PinScreenRuntime =
    remember {
        PinScreenRuntime(
            fetchLinkCardPreview = { null },
        )
    }

@Composable
actual fun PinHtmlWebViewContent(html: String) = Unit

actual fun supportsPinHtmlWebView(): Boolean = false

@Composable
actual fun rememberBlocklistSettingsPlatformRuntime(
    userMessages: UserMessageSink,
): BlocklistSettingsRuntime = remember(userMessages) {
    BlocklistSettingsRuntime(
        requestImport = { callback -> userMessages.showMessage("导入规则暂未实现") },
        exportRules = { "" },
    )
}

@Composable
actual fun rememberZhihuHttpClient(): HttpClient {
    val store = remember { IosAccountStore() }
    val session = remember { store.load() }
    return remember(store, session) { store.createHttpClient(session.cookies.toMutableMap()) }
}

internal fun openIosUrl(url: String) {
    val nsUrl = NSURL.URLWithString(url) ?: return
    UIApplication.sharedApplication.openURL(nsUrl)
}

@Composable
actual fun QuestionDetailWebViewContent(questionId: Long, html: String) = Unit

actual fun supportsQuestionDetailWebView(): Boolean = false

actual fun Modifier.questionSelectionWorkaround(): Modifier = this
