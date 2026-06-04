import SwiftUI

@main
struct ZhihuApp: App {
    @StateObject private var appState = AppState()

    var body: some Scene {
        WindowGroup {
            ComposeView(appState: appState)
                .ignoresSafeArea()
                .onOpenURL { url in
                    appState.handleDeepLink(url)
                }
        }
    }
}
