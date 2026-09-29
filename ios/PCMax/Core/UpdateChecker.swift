import Foundation

struct UpdateInfo {
    let build: Int
    let downloadURL: URL
}

enum UpdateResult {
    case available(UpdateInfo)
    case upToDate
}

/// بررسی آپدیت نسخه‌ی iPhone از همان صفحه‌ی Releases.
/// فقط فایل‌های `PCMax-ios-<build>.ipa` بررسی می‌شوند (فایل APK اندروید نادیده گرفته می‌شود)،
/// و شماره‌ی build از اسم همین فایل خوانده می‌شود، نه از اسم Release.
enum UpdateChecker {
    private static let api = URL(
        string: "https://api.github.com/repos/alitanhaii1970-commits/game-settings-app-test/releases/latest"
    )!

    static var localBuild: Int {
        Int(Bundle.main.object(forInfoDictionaryKey: "CFBundleVersion") as? String ?? "0") ?? 0
    }

    static var versionName: String {
        Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? "1.0"
    }

    static func check() async throws -> UpdateResult {
        var request = URLRequest(url: api)
        request.setValue("application/vnd.github+json", forHTTPHeaderField: "Accept")
        request.timeoutInterval = 12

        let (data, response) = try await URLSession.shared.data(for: request)
        guard let http = response as? HTTPURLResponse, http.statusCode == 200 else {
            throw URLError(.badServerResponse)
        }
        guard let json = try JSONSerialization.jsonObject(with: data) as? [String: Any],
              let assets = json["assets"] as? [[String: Any]] else {
            throw URLError(.cannotParseResponse)
        }

        let regex = try NSRegularExpression(pattern: "^PCMax-ios-(\\d+)\\.ipa$")
        var best: (build: Int, url: String)? = nil

        for asset in assets {
            guard let name = asset["name"] as? String,
                  let link = asset["browser_download_url"] as? String else { continue }
            let range = NSRange(name.startIndex..., in: name)
            guard let match = regex.firstMatch(in: name, range: range),
                  let numRange = Range(match.range(at: 1), in: name),
                  let build = Int(name[numRange]) else { continue }
            if best == nil || build > best!.build { best = (build, link) }
        }

        if let best = best, best.build > localBuild, let url = URL(string: best.url) {
            return .available(UpdateInfo(build: best.build, downloadURL: url))
        }
        return .upToDate
    }
}
