import SwiftUI

@MainActor
final class AppState: ObservableObject {

    @Published var lang: Lang {
        didSet { UserDefaults.standard.set(lang.rawValue, forKey: "language") }
    }
    @Published var fontId: String {
        didSet { UserDefaults.standard.set(fontId, forKey: "app_font") }
    }
    @Published var tier: String {
        didSet { UserDefaults.standard.set(tier, forKey: "system_tier") }
    }
    @Published var onboardingDone: Bool {
        didSet { UserDefaults.standard.set(onboardingDone, forKey: "onboarding_done") }
    }

    @Published var games: [Game] = []
    @Published var isLoading = false
    @Published var emptyMessage: String? = nil
    @Published var toastMessage: String? = nil

    private var hasEverLoaded: Bool {
        get { UserDefaults.standard.bool(forKey: "has_loaded_games") }
        set { UserDefaults.standard.set(newValue, forKey: "has_loaded_games") }
    }

    private let repo = GameRepository()
    private var toastTask: Task<Void, Never>?

    static let tierWeak = "weak"
    static let tierMedium = "medium"
    static let tierStrong = "strong"

    init() {
        let d = UserDefaults.standard
        lang = Lang(rawValue: d.string(forKey: "language") ?? "fa") ?? .fa
        fontId = d.string(forKey: "app_font") ?? AppFonts.systemId
        tier = d.string(forKey: "system_tier") ?? ""
        onboardingDone = d.bool(forKey: "onboarding_done")
    }

    func t(_ key: String) -> String { L.t(key, lang) }

    func finishOnboarding(lang: Lang, tier: String) {
        self.lang = lang
        self.tier = tier
        onboardingDone = true
    }

    func showToast(_ text: String) {
        toastTask?.cancel()
        toastMessage = text
        toastTask = Task {
            try? await Task.sleep(nanoseconds: 2_200_000_000)
            if !Task.isCancelled { toastMessage = nil }
        }
    }

    /// بار اول واقعی برنامه از سرور می‌خونه؛ دفعات بعدی فقط از کش محلی، تا وقتی
    /// خود کاربر دکمه‌ی رفرش رو بزنه — دقیقاً مثل نسخه‌ی اندروید.
    func loadInitial() async {
        if hasEverLoaded, let cached = repo.loadCache() {
            games = cached
            return
        }
        await refresh(forceServer: true)
    }

    func refresh(forceServer: Bool) async {
        isLoading = true
        emptyMessage = nil
        defer { isLoading = false }

        do {
            let fresh = try await repo.fetchFromServer()
            games = fresh
            repo.saveCache(fresh)
            if forceServer {
                hasEverLoaded = true
                showToast(t("list_updated"))
            }
        } catch {
            if games.isEmpty {
                emptyMessage = t("offline_empty")
            } else {
                showToast(t("offline_toast"))
            }
        }
    }
}
