import SwiftUI
import UIKit
import CryptoKit

/// کش دو لایه‌ی عکس (حافظه + دیسک) — عکس‌های بازی‌ها فقط یک‌بار دانلود می‌شوند.
final class ImageCache {
    static let shared = ImageCache()

    private let memory = NSCache<NSString, UIImage>()
    private let directory: URL
    private let session: URLSession

    private init() {
        let base = FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask)[0]
            .appendingPathComponent("game_images", isDirectory: true)
        try? FileManager.default.createDirectory(at: base, withIntermediateDirectories: true)
        directory = base
        memory.totalCostLimit = 80 * 1024 * 1024

        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 20
        session = URLSession(configuration: config)
    }

    private func fileURL(for urlString: String) -> URL {
        let digest = SHA256.hash(data: Data(urlString.utf8))
        let name = digest.map { String(format: "%02x", $0) }.joined()
        return directory.appendingPathComponent(name)
    }

    func image(for urlString: String, maxPixel: CGFloat?) async -> UIImage? {
        let memKey = "\(urlString)#\(Int(maxPixel ?? 0))" as NSString
        if let cached = memory.object(forKey: memKey) { return cached }

        let file = fileURL(for: urlString)
        var data: Data? = try? Data(contentsOf: file)

        if data == nil {
            guard let url = URL(string: urlString.trimmingCharacters(in: .whitespacesAndNewlines)) else { return nil }
            do {
                let (downloaded, response) = try await session.data(from: url)
                if let http = response as? HTTPURLResponse, http.statusCode != 200 { return nil }
                data = downloaded
                try? downloaded.write(to: file, options: .atomic)
            } catch {
                return nil
            }
        }

        guard let bytes = data, var image = UIImage(data: bytes) else { return nil }

        if let maxPixel = maxPixel {
            let longest = max(image.size.width, image.size.height)
            if longest > maxPixel {
                let scale = maxPixel / longest
                let target = CGSize(width: image.size.width * scale, height: image.size.height * scale)
                if let thumb = await image.byPreparingThumbnail(ofSize: target) {
                    image = thumb
                }
            }
        }

        memory.setObject(image, forKey: memKey, cost: Int(image.size.width * image.size.height * 4))
        return image
    }
}

/// نمایش عکس از اینترنت با کش، انیمیشن ظاهر شدن و حالت خطا
struct RemoteImage: View {
    let url: String
    var maxPixel: CGFloat? = nil
    var contentMode: ContentMode = .fill
    var showBackground: Bool = true

    @State private var image: UIImage?
    @State private var failed = false

    var body: some View {
        ZStack {
            if showBackground { Theme.surfaceAlt }
            if let image = image {
                Image(uiImage: image)
                    .resizable()
                    .aspectRatio(contentMode: contentMode)
                    .transition(.opacity)
            } else if failed {
                Image(systemName: "photo")
                    .font(.system(size: 22))
                    .foregroundColor(Theme.textSecondary)
            } else {
                ProgressView().tint(Theme.textSecondary)
            }
        }
        .task(id: url) {
            image = nil
            failed = false
            if let loaded = await ImageCache.shared.image(for: url, maxPixel: maxPixel) {
                withAnimation(.easeOut(duration: 0.35)) { image = loaded }
            } else {
                failed = true
            }
        }
    }
}
