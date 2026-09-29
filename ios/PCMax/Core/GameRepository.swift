import Foundation

/// تمام ارتباط با Firestore از اینجاست (از طریق REST، بدون نیاز به SDK).
/// اگر روزی به سرور اختصاصی مهاجرت کردیم، فقط همین فایل عوض می‌شود.
final class GameRepository {

    private let projectId = "pc-max-a0a4b"
    private let apiKey = "AIzaSyDloW_gBy_P7WZEBlEEa9wzOxX-PMmlvkQ"

    private let session: URLSession = {
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 15
        config.requestCachePolicy = .reloadIgnoringLocalCacheData
        return URLSession(configuration: config)
    }()

    // MARK: کش محلی (مثل کش Firestore در اندروید)

    private var cacheURL: URL {
        let dir = FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask)[0]
        try? FileManager.default.createDirectory(at: dir, withIntermediateDirectories: true)
        return dir.appendingPathComponent("games_cache.json")
    }

    func loadCache() -> [Game]? {
        guard let data = try? Data(contentsOf: cacheURL),
              let games = try? JSONDecoder().decode([Game].self, from: data),
              !games.isEmpty else { return nil }
        return games
    }

    func saveCache(_ games: [Game]) {
        if let data = try? JSONEncoder().encode(games) {
            try? data.write(to: cacheURL, options: .atomic)
        }
    }

    // MARK: دریافت از سرور

    func fetchFromServer() async throws -> [Game] {
        var all: [Game] = []
        var pageToken: String? = nil
        var useKey = true

        repeat {
            let page = try await fetchPageWithFallback(token: pageToken, useKey: &useKey)
            all.append(contentsOf: page.docs)
            pageToken = page.next
        } while pageToken != nil

        return all.sorted { $0.name.lowercased() < $1.name.lowercased() }
    }

    /// اگر کلید API برای iOS محدود شده باشد (خطای 400/403)، یک‌بار بدون کلید
    /// (با تکیه بر Security Rules عمومی) دوباره تلاش می‌کند.
    private func fetchPageWithFallback(token: String?, useKey: inout Bool) async throws -> (docs: [Game], next: String?) {
        do {
            return try await fetchPage(token: token, useKey: useKey)
        } catch RepositoryError.http(let code) where useKey && (code == 400 || code == 403) {
            useKey = false
            return try await fetchPage(token: token, useKey: false)
        }
    }

    private func fetchPage(token: String?, useKey: Bool) async throws -> (docs: [Game], next: String?) {
        var comps = URLComponents(
            string: "https://firestore.googleapis.com/v1/projects/\(projectId)/databases/(default)/documents/games"
        )!
        var items = [URLQueryItem(name: "pageSize", value: "300")]
        if let token = token { items.append(URLQueryItem(name: "pageToken", value: token)) }
        if useKey { items.append(URLQueryItem(name: "key", value: apiKey)) }
        comps.queryItems = items

        guard let url = comps.url else { throw RepositoryError.badURL }
        let (data, response) = try await session.data(from: url)
        guard let http = response as? HTTPURLResponse else { throw RepositoryError.badURL }
        guard http.statusCode == 200 else { throw RepositoryError.http(http.statusCode) }

        let root = (try? JSONSerialization.jsonObject(with: data)) as? [String: Any] ?? [:]
        let documents = root["documents"] as? [[String: Any]] ?? []
        let next = root["nextPageToken"] as? String

        var games: [Game] = []
        for doc in documents {
            guard let fullName = doc["name"] as? String else { continue }
            let id = fullName.split(separator: "/").last.map(String.init) ?? fullName
            let fields = doc["fields"] as? [String: Any] ?? [:]
            games.append(Game(
                id: id,
                name: Self.string(fields, "name"),
                imageUrl: Self.string(fields, "imageUrl"),
                settingsGreen: Self.string(fields, "settingsGreen"),
                settingsYellow: Self.string(fields, "settingsYellow"),
                youtubeUrl: Self.string(fields, "youtubeUrl"),
                showYoutubeButton: Self.bool(fields, "showYoutubeButton"),
                updatedAt: Self.int(fields, "updatedAt")
            ))
        }
        return (games, next)
    }

    // MARK: خواندن نوع‌های Firestore REST

    private static func string(_ f: [String: Any], _ key: String) -> String {
        (f[key] as? [String: Any])?["stringValue"] as? String ?? ""
    }

    private static func bool(_ f: [String: Any], _ key: String) -> Bool {
        (f[key] as? [String: Any])?["booleanValue"] as? Bool ?? false
    }

    private static func int(_ f: [String: Any], _ key: String) -> Int64 {
        guard let v = (f[key] as? [String: Any])?["integerValue"] else { return 0 }
        if let s = v as? String { return Int64(s) ?? 0 }
        if let n = v as? NSNumber { return n.int64Value }
        return 0
    }
}

enum RepositoryError: Error {
    case http(Int)
    case badURL
}
