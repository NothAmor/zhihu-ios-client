# Zhihu++ iOS

[![License: AGPL v3](https://img.shields.io/badge/License-AGPL_v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)

注重隐私、无广告的第三方知乎 iOS 客户端。

基于 [Zhihu++ Android](https://github.com/zly2006/zhihu-plus-plus) 的 Kotlin Multiplatform (KMP) + Compose Multiplatform 架构 1:1 复刻。

## 功能

- **信息流与推荐**：首页推荐（Web/Android/本地/混合模式）、关注页、热榜、知乎日报、搜索
- **内容浏览**：阅读回答/文章、问题详情、想法(Pin)、收藏夹、历史记录
- **社区互动**：评论、点赞、通知
- **屏蔽系统**：屏蔽词(含正则)、屏蔽用户、屏蔽话题
- **内容导出**：PDF/图片/Markdown/HTML
- **本地推荐算法**：完全独立的本地推荐，避免信息茧房
- **无广告、去盐选**

## 项目结构

```
zhihu-ios-client/
├── shared/               # KMP 共享模块 (Compose Multiplatform)
│   ├── commonMain/       # 跨平台 UI + 业务逻辑
│   ├── nativeMain/       # iOS/macOS 共享平台实现
│   ├── iosMain/          # iOS 专用平台实现
│   ├── androidMain/      # Android 平台实现
│   └── jvmMain/          # JVM/Desktop 平台实现
├── ZhihuApp/             # Xcode 项目 (SwiftUI 宿主)
│   └── ZhihuApp/
│       ├── App.swift
│       ├── ContentView.swift
│       ├── AppState.swift
│       └── Info.plist
├── build.gradle.kts      # 根构建脚本
├── settings.gradle.kts   # 项目设置
└── Makefile              # 构建命令
```

## 构建要求

- **Java 17+**: `brew install openjdk@17`
- **Xcode 16+** (macOS)
- **Android SDK** (仅 androidMain 编译需要；纯 iOS 构建可跳过)

## 构建与运行

### 1. 构建 KMP Framework

```bash
# iOS 模拟器 (debug)
make shared-framework

# iOS 真机
make shared-framework-device
```

### 2. 在 Xcode 中运行

```bash
# 打开 Xcode 项目
make xcode-open

# 或在 Xcode 中选择 ZhihuApp scheme，选择模拟器，点击 Run
```

### 3. 命令行构建

```bash
make xcode-build
```

## 开发状态

- [x] 项目骨架搭建
- [x] KMP shared 模块集成
- [x] Xcode SwiftUI 宿主工程
- [x] 核心平台 expect/actual 实现
- [ ] 登录系统 (Cookie 管理)
- [ ] WebView 内容渲染
- [ ] TTS 朗读
- [ ] 内容导出
- [ ] 表情包
- [ ] QR 扫码

## 致谢

本项目基于 [zly2006/zhihu-plus-plus](https://github.com/zly2006/zhihu-plus-plus)，遵循 AGPL v3 协议。

## License

AGPL v3 — 详见 [LICENSE](LICENSE)
