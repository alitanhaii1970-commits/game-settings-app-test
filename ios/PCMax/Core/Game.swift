import Foundation

/// مدل هر بازی — فیلدها دقیقاً همان فیلدهای Firestore هستند (مثل نسخه‌ی اندروید)
struct Game: Identifiable, Codable, Hashable {
    var id: String
    var name: String
    var imageUrl: String
    var settingsGreen: String
    var settingsYellow: String
    var youtubeUrl: String
    var showYoutubeButton: Bool
    var updatedAt: Int64

    /// حالت «فقط یوتیوب» — همان منطق اندروید
    var hasYoutubeMode: Bool {
        let trimmed = youtubeUrl.trimmingCharacters(in: .whitespacesAndNewlines)
        return showYoutubeButton && !trimmed.isEmpty &&
            (trimmed.contains("youtube") || trimmed.hasPrefix("http"))
    }

    var hasGreen: Bool { !settingsGreen.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty }
    var hasYellow: Bool { !settingsYellow.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty }
}
