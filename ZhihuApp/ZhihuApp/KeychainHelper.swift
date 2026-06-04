import Security
import Foundation

/// Simple Keychain wrapper for secure storage of cookies and credentials
enum KeychainHelper {
    private static let service = "me.nothamor.zhihu-ios-client"

    static func save(key: String, data: Data) -> Bool {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: key,
            kSecValueData as String: data,
        ]

        // Delete existing item first
        SecItemDelete(query as CFDictionary)

        // Add new item
        let status = SecItemAdd(query as CFDictionary, nil)
        return status == errSecSuccess
    }

    static func load(key: String) -> Data? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: key,
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne,
        ]

        var result: AnyObject?
        let status = SecItemCopyMatching(query as CFDictionary, &result)
        guard status == errSecSuccess else { return nil }
        return result as? Data
    }

    static func delete(key: String) -> Bool {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: key,
        ]
        let status = SecItemDelete(query as CFDictionary)
        return status == errSecSuccess
    }

    // MARK: - Convenience

    static func saveString(key: String, value: String) -> Bool {
        guard let data = value.data(using: .utf8) else { return false }
        return save(key: key, data: data)
    }

    static func loadString(key: String) -> String? {
        guard let data = load(key: key) else { return nil }
        return String(data: data, encoding: .utf8)
    }

    // MARK: - Cookie storage

    static func saveCookies(_ cookies: [HTTPCookie]) {
        let cookieData = cookies.map { cookie -> [String: Any] in
            [
                "name": cookie.name,
                "value": cookie.value,
                "domain": cookie.domain,
                "path": cookie.path,
                "expires": cookie.expiresDate?.timeIntervalSince1970 ?? 0,
                "isSecure": cookie.isSecure,
            ]
        }
        if let data = try? JSONSerialization.data(withJSONObject: cookieData) {
            _ = save(key: "zhihu_cookies", data: data)
        }
    }

    static func loadCookies() -> [HTTPCookie] {
        guard let data = load(key: "zhihu_cookies"),
              let cookieArray = try? JSONSerialization.jsonObject(with: data) as? [[String: Any]] else {
            return []
        }
        return cookieArray.compactMap { dict in
            let props: [HTTPCookiePropertyKey: Any] = [
                .name: dict["name"] as? String ?? "",
                .value: dict["value"] as? String ?? "",
                .domain: dict["domain"] as? String ?? "",
                .path: dict["path"] as? String ?? "/",
                .secure: dict["isSecure"] as? Bool ?? true,
            ]
            return HTTPCookie(properties: props)
        }
    }
}
