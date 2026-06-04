import SwiftUI
import Shared

struct ComposeView: UIViewControllerRepresentable {
    @ObservedObject var appState: AppState

    func makeUIViewController(context: Context) -> UIViewController {
        // Call the Kotlin Multiplatform entry point
        // MainViewControllerKt.MainViewController() comes from iosMain
        let vc = MainViewControllerKt.MainViewController()
        return vc
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // Compose handles its own state updates
    }
}
