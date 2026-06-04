import Foundation
import Combine
import UIKit

/// Shared application state for bridge between Swift and Kotlin
class AppState: ObservableObject {
    @Published var deepLinkURL: URL?

    func handleDeepLink(_ url: URL) {
        deepLinkURL = url
        // Forward to Kotlin side via a notification or callback
        NotificationCenter.default.post(
            name: .zhihuDeepLinkReceived,
            object: nil,
            userInfo: ["url": url]
        )
    }
}

extension Notification.Name {
    static let zhihuDeepLinkReceived = Notification.Name("zhihuDeepLinkReceived")
}
