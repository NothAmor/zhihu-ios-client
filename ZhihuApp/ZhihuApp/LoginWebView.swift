import SwiftUI
import WebKit

/// WKWebView wrapper for Zhihu login
/// Displays the Zhihu sign-in page and extracts cookies after successful login
struct LoginWebView: UIViewRepresentable {
    let onCookiesExtracted: ([HTTPCookie]) -> Void
    let onDismiss: () -> Void

    func makeUIView(context: Context) -> WKWebView {
        let config = WKWebViewConfiguration()
        config.websiteDataStore = WKWebsiteDataStore.default()

        let webView = WKWebView(frame: .zero, configuration: config)
        webView.navigationDelegate = context.coordinator
        webView.customUserAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 18_0 like Mac OS X) AppleWebKit/605.1.15"

        // Load Zhihu sign-in page
        if let url = URL(string: "https://www.zhihu.com/signin") {
            let request = URLRequest(url: url)
            webView.load(request)
        }

        return webView
    }

    func updateUIView(_ uiView: WKWebView, context: Context) {}

    func makeCoordinator() -> Coordinator {
        Coordinator(onCookiesExtracted: onCookiesExtracted)
    }

    class Coordinator: NSObject, WKNavigationDelegate {
        let onCookiesExtracted: ([HTTPCookie]) -> Void

        init(onCookiesExtracted: @escaping ([HTTPCookie]) -> Void) {
            self.onCookiesExtracted = onCookiesExtracted
        }

        func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
            guard let url = webView.url else { return }

            // Check if user has reached the home page (indicates successful login)
            let isLoggedIn = url.absoluteString.contains("zhihu.com") &&
                !url.absoluteString.contains("/signin") &&
                !url.absoluteString.contains("/login")

            if isLoggedIn {
                // Extract cookies
                WKWebsiteDataStore.default().httpCookieStore.getAllCookies { cookies in
                    let zhihuCookies = cookies.filter { cookie in
                        cookie.domain.contains("zhihu.com")
                    }
                    if !zhihuCookies.isEmpty {
                        self.onCookiesExtracted(zhihuCookies)
                    }
                }
            }
        }
    }
}

/// Manual cookie input view for advanced users
struct ManualCookieInputView: View {
    @State private var cookieText: String = ""
    @State private var userAgent: String = "Mozilla/5.0 (iPhone; CPU iPhone OS 18_0 like Mac OS X) AppleWebKit/605.1.15"
    let onSave: (String, String) -> Void
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Cookie 字符串")) {
                    TextEditor(text: $cookieText)
                        .frame(minHeight: 200)
                        .font(.system(.caption, design: .monospaced))
                }
                Section(header: Text("User-Agent")) {
                    TextField("User-Agent", text: $userAgent)
                        .font(.caption)
                }
                Section {
                    Button("保存并登录") {
                        onSave(cookieText, userAgent)
                        dismiss()
                    }
                    .disabled(cookieText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                }
            }
            .navigationTitle("手动设置 Cookie")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("取消") { dismiss() }
                }
            }
        }
    }
}

/// QR Code login view
struct QRLoginView: View {
    @State private var qrImage: UIImage?
    @State private var statusText: String = "请使用知乎 App 扫描二维码"
    let onLoginSuccess: ([HTTPCookie]) -> Void
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationView {
            VStack(spacing: 20) {
                if let qrImage = qrImage {
                    Image(uiImage: qrImage)
                        .resizable()
                        .interpolation(.none)
                        .aspectRatio(contentMode: .fit)
                        .frame(width: 250, height: 250)
                        .padding()
                } else {
                    ProgressView()
                        .frame(width: 250, height: 250)
                }

                Text(statusText)
                    .foregroundColor(.secondary)

                Button("使用网页登录") {
                    dismiss()
                    // Present web login
                }
            }
            .navigationTitle("扫码登录")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("取消") { dismiss() }
                }
            }
            .task {
                await loadQRCode()
            }
        }
    }

    private func loadQRCode() async {
        // QR code loading would call the Zhihu API
        // For now, placeholder
        statusText = "扫码登录功能开发中"
    }
}
