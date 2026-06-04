import AVFoundation
import UIKit
import Photos

// MARK: - TTS (Text-to-Speech)

/// iOS TTS engine using AVSpeechSynthesizer
final class IOSTTSEngine: NSObject, ObservableObject, AVSpeechSynthesizerDelegate {
    static let shared = IOSTTSEngine()

    private let synthesizer = AVSpeechSynthesizer()
    private var textChunks: [String] = []
    private var currentChunkIndex = 0
    private var onChunkComplete: (() -> Void)?

    @Published var isSpeaking = false
    @Published var isPaused = false

    override private init() {
        super.init()
        synthesizer.delegate = self
    }

    /// Speak text in chunks (similar to Android's splitTextIntoChunks)
    func speak(title: String, content: String) {
        stop()

        let fullText = "\(title)。\(stripHTML(content))"
        textChunks = splitIntoChunks(fullText, maxLength: 200)
        currentChunkIndex = 0

        speakNextChunk()
    }

    func stop() {
        synthesizer.stopSpeaking(at: .immediate)
        isSpeaking = false
        isPaused = false
        textChunks = []
    }

    func pause() {
        synthesizer.pauseSpeaking(at: .immediate)
        isPaused = true
    }

    func resume() {
        synthesizer.continueSpeaking()
        isPaused = false
    }

    private func speakNextChunk() {
        guard currentChunkIndex < textChunks.count else {
            isSpeaking = false
            return
        }

        let utterance = AVSpeechUtterance(string: textChunks[currentChunkIndex])
        utterance.voice = AVSpeechSynthesisVoice(language: "zh-CN")
        utterance.rate = 0.5 // Slower rate for Chinese
        utterance.pitchMultiplier = 1.0

        isSpeaking = true
        synthesizer.speak(utterance)
    }

    // MARK: - AVSpeechSynthesizerDelegate

    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didFinish utterance: AVSpeechUtterance) {
        currentChunkIndex += 1
        DispatchQueue.main.async { [weak self] in
            self?.speakNextChunk()
        }
    }

    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didCancel utterance: AVSpeechUtterance) {
        DispatchQueue.main.async { [weak self] in
            self?.isSpeaking = false
        }
    }
}

// MARK: - Image Save & Share

/// Save an image to the Photos library
func saveImageToPhotos(urlString: String) async throws {
    guard let url = URL(string: urlString) else {
        throw NSError(domain: "ZhihuApp", code: 1, userInfo: [NSLocalizedDescriptionKey: "Invalid URL"])
    }

    let (data, _) = try await URLSession.shared.data(from: url)
    guard let image = UIImage(data: data) else {
        throw NSError(domain: "ZhihuApp", code: 2, userInfo: [NSLocalizedDescriptionKey: "Invalid image data"])
    }

    try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<Void, Error>) in
        PHPhotoLibrary.requestAuthorization { status in
            guard status == .authorized else {
                continuation.resume(throwing: NSError(domain: "ZhihuApp", code: 3,
                    userInfo: [NSLocalizedDescriptionKey: "Photo library access denied"]))
                return
            }
            UIImageWriteToSavedPhotosAlbum(image, nil, nil, nil)
            continuation.resume()
        }
    }
}

/// Share content using the system Share Sheet
func shareContent(items: [Any], from viewController: UIViewController) {
    let activityVC = UIActivityViewController(activityItems: items, applicationActivities: nil)
    viewController.present(activityVC, animated: true)
}

// MARK: - Deep Link Handling

/// Parse Zhihu URLs into their content type and ID
enum ZhihuContentType {
    case answer(id: Int64)
    case article(id: Int64)
    case question(id: Int64)
    case person(id: String)
    case pin(id: Int64)
    case search(query: String)
    case unknown

    init?(url: URL) {
        guard let host = url.host else { return nil }

        if url.scheme == "zhihu" {
            let path = url.path.trimmingCharacters(in: CharacterSet(charactersIn: "/"))
            let parts = path.split(separator: "/")
            switch url.host {
            case "answers" where parts.count >= 1:
                if let id = Int64(parts[0]) { self = .answer(id: id) }
            case "questions" where parts.count >= 1:
                if let id = Int64(parts[0]) { self = .question(id: id) }
            case "articles" where parts.count >= 1:
                if let id = Int64(parts[0]) { self = .article(id: id) }
            case "pin" where parts.count >= 1:
                if let id = Int64(parts[0]) { self = .pin(id: id) }
            case "people" where parts.count >= 1:
                self = .person(id: String(parts[0]))
            case "search":
                self = .search(query: "")
            default: return nil
            }
        } else if host.contains("zhihu.com") || host == "zhuanlan.zhihu.com" {
            let path = url.path
            let parts = path.split(separator: "/")

            if path.hasPrefix("/question/") && path.contains("/answer/"),
               let answerIdx = parts.firstIndex(of: "answer"),
               answerIdx + 1 < parts.count,
               let id = Int64(parts[answerIdx + 1]) {
                self = .answer(id: id)
            } else if path.hasPrefix("/question/"), parts.count >= 2,
                      let id = Int64(parts[1]) {
                self = .question(id: id)
            } else if path.hasPrefix("/people/"), parts.count >= 2 {
                self = .person(id: String(parts[1]))
            } else if (path.hasPrefix("/p/") || path.hasPrefix("/oia/articles/")),
                      parts.count >= 2,
                      let id = Int64(parts[1]) {
                self = .article(id: id)
            } else if path.hasPrefix("/pin/"), parts.count >= 2,
                      let id = Int64(parts[1]) {
                self = .pin(id: id)
            } else { return nil }
        } else { return nil }
    }
}

// MARK: - Utility

private func stripHTML(_ html: String) -> String {
    guard let data = html.data(using: .utf8) else { return html }
    if let plain = try? NSAttributedString(
        data: data,
        options: [.documentType: NSAttributedString.DocumentType.html],
        documentAttributes: nil
    ).string {
        return plain
    }
    return html
}

private func splitIntoChunks(_ text: String, maxLength: Int) -> [String] {
    guard text.count > maxLength else { return [text] }

    var chunks: [String] = []
    var currentPos = text.startIndex

    while currentPos < text.endIndex {
        let endPos = text.index(currentPos, offsetBy: min(maxLength, text.distance(from: currentPos, to: text.endIndex)))
        var chunk = String(text[currentPos..<endPos])

        // Try to break at sentence boundaries
        if endPos < text.endIndex {
            let separators: [Character] = ["。", "！", "？", ".", "!", "?"]
            if let lastSep = chunk.lastIndex(where: { separators.contains($0) }),
               chunk.distance(from: chunk.startIndex, to: lastSep) > chunk.count / 2 {
                chunk = String(chunk[...lastSep])
            }
        }

        chunks.append(chunk.trimmingCharacters(in: .whitespaces))
        currentPos = text.index(currentPos, offsetBy: chunk.count)
    }

    return chunks
}
