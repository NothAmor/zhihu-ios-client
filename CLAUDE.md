# Zhihu++ iOS Agent Instructions

本项目是 Zhihu++ iOS 客户端，从 [Android 版本](https://github.com/zly2006/zhihu-plus-plus) 1:1 复刻。

## 架构

- **shared/commonMain**: 跨平台 Compose Multiplatform UI + 业务逻辑（从 Android 项目直接复用）
- **shared/nativeMain**: iOS/macOS 共享平台实现 (Kotlin/Native)
- **shared/iosMain**: iOS 专用平台实现
- **ZhihuApp/**: Xcode SwiftUI 宿主 App

## 关键技术栈

- Kotlin Multiplatform (KMP) + Compose Multiplatform
- Ktor (Darwin engine) - HTTP 网络请求
- Room - SQLite 数据库
- Coil3 - 图片加载
- Material 3 - UI 组件
- NSUserDefaults - 设置持久化

## 平台实现模式

`commonMain` 中声明 `expect`，`nativeMain`/`iosMain` 中提供 `actual`。

## 关键 expect/actual 文件映射

| commonMain expect | nativeMain actual |
|---|---|
| `PlatformCapabilities.kt` (expect) | `NativePlatformCapabilities.kt` (actual) |
| `UiSupportFiles.kt` (expect) | `NativeUiRuntimes.kt` (actual) |
| `ZhihuShared.kt` (expect) | `NativeZhihuApp.kt` (actual) |
| `PaginationViewModel.kt` (expect) | `PaginationEnvironment.native.kt` (actual) |

## 构建与运行

```bash
# 构建 KMP framework
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64

# 打开 Xcode
open ZhihuApp/ZhihuApp.xcodeproj
```

## 注意事项

- 不修改 commonMain 中的业务逻辑，只补齐 expect/actual
- iOS 使用 `NSUserDefaults` 而非 Android SharedPreferences
- iOS 使用 `IosAccountStore` (Keychain + 文件) 管理账号
- Rust zse96 签名库需额外集成（当前不支持）
- WebView 使用 Markdown fallback 而非 WKWebView
