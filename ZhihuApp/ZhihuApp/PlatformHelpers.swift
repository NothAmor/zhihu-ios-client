import AVFoundation
import UIKit
import Photos

// MARK: - TTS (Text-to-Speech)

/// iOS TTS engine using AVSpeechSynthesizer
final class IOSTTSEngine: NSObject, ObservableObject, AVSpeechSynthesizerDelegate, @unchecked Sendable {
    static let shared = IOSTTSEngine()

    private let synthesizer = AVSpeechSynthesizer()
    private var textChunks: [String] = []
    private var currentChunkIndex = 0

    @Published var isSpeaking = false
    @Published var isPaused = false

    override private init() {
        super.init()
        synthesizer.delegate = self
    }

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
        utterance.rate = 0.5
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

func shareContent(items: [Any], from viewController: UIViewController) {
    let activityVC = UIActivityViewController(activityItems: items, applicationActivities: nil)
    viewController.present(activityVC, animated: true)
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
