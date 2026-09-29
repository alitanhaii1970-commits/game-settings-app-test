import SwiftUI

/// مسیر ورود اولیه — دقیقاً مثل اندروید همیشه به فارسی نمایش داده می‌شود
/// (چون در نسخه‌ی اندروید هم این مراحل پیش از اعمال زبان انتخابی اجرا می‌شوند)
/// و فقط انتخاب نهایی کاربر در پایان مسیر ذخیره و اعمال می‌گردد.
struct OnboardingView: View {
    @EnvironmentObject var state: AppState

    @State private var page = 0
    @State private var selectedLang: Lang = .fa
    @State private var selectedTier: String = AppState.tierMedium

    private func t(_ key: String) -> String { L.t(key, .fa) }

    var body: some View {
        VStack(spacing: 0) {
            ZStack {
                Group {
                    switch page {
                    case 0: welcomePage
                    case 1: languagePage
                    default: tierPage
                    }
                }
                .transition(.asymmetric(
                    insertion: .move(edge: .trailing).combined(with: .opacity),
                    removal: .move(edge: .leading).combined(with: .opacity)
                ))
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .animation(.easeOut(duration: 0.32), value: page)

            dots

            Button {
                Haptics.light()
                if page < 2 {
                    withAnimation { page += 1 }
                } else {
                    Haptics.success()
                    state.finishOnboarding(lang: selectedLang, tier: selectedTier)
                }
            } label: {
                Text(page == 2 ? t("onboard_finish") : t("onboard_next"))
                    .appFont(16, bold: true)
            }
            .buttonStyle(PrimaryButtonStyle())
            .padding(.horizontal, 28)
            .padding(.bottom, 32)
        }
        .background(Theme.background.ignoresSafeArea())
        .environment(\.layoutDirection, .rightToLeft)
    }

    private var dots: some View {
        HStack(spacing: 6) {
            ForEach(0..<3) { i in
                Capsule()
                    .fill(i == page ? Theme.red : Theme.border)
                    .frame(width: i == page ? 22 : 8, height: 8)
                    .animation(.easeOut(duration: 0.25), value: page)
            }
        }
        .padding(.bottom, 20)
    }

    private var welcomePage: some View {
        VStack(spacing: 28) {
            Image("Logo")
                .resizable()
                .aspectRatio(contentMode: .fill)
                .frame(width: 120, height: 120)
                .clipShape(RoundedRectangle(cornerRadius: 26, style: .continuous))

            VStack(spacing: 10) {
                Text(t("onboard_welcome_title"))
                    .appFont(24, bold: true, style: .title)
                    .foregroundColor(Theme.textPrimary)
                Text(t("onboard_welcome_subtitle"))
                    .appFont(15)
                    .foregroundColor(Theme.textSecondary)
            }
            .multilineTextAlignment(.center)
        }
        .padding(32)
    }

    private var languagePage: some View {
        VStack(spacing: 14) {
            pageHeader(title: t("onboard_lang_title"), subtitle: t("onboard_lang_subtitle"))

            OptionCard(title: t("lang_fa"), selected: selectedLang == .fa, action: { selectedLang = .fa }) {
                EmptyView()
            }
            OptionCard(title: t("lang_en"), selected: selectedLang == .en, action: { selectedLang = .en }) {
                EmptyView()
            }
            Spacer()
        }
        .padding(.horizontal, 28)
        .padding(.top, 56)
    }

    private var tierPage: some View {
        VStack(spacing: 12) {
            pageHeader(title: t("onboard_tier_title"), subtitle: t("onboard_tier_subtitle"))

            tierOption(id: AppState.tierWeak, label: t("tier_weak"), colors: [Color(hex: 0xE31E24), Color(hex: 0x8E1216)])
            tierOption(id: AppState.tierMedium, label: t("tier_medium"), colors: [Color(hex: 0xF5C518), Color(hex: 0xB38600)])
            tierOption(id: AppState.tierStrong, label: t("tier_strong"), colors: [Color(hex: 0x2ECC71), Color(hex: 0x1E9E52)])
            Spacer()
        }
        .padding(.horizontal, 28)
        .padding(.top, 56)
    }

    private func tierOption(id: String, label: String, colors: [Color]) -> some View {
        OptionCard(title: label, selected: selectedTier == id, action: { selectedTier = id }) {
            RoundedRectangle(cornerRadius: 14, style: .continuous)
                .fill(LinearGradient(colors: colors, startPoint: .topLeading, endPoint: .bottomTrailing))
                .frame(width: 44, height: 44)
        }
    }

    private func pageHeader(title: String, subtitle: String) -> some View {
        VStack(spacing: 8) {
            Text(title)
                .appFont(22, bold: true, style: .title2)
                .foregroundColor(Theme.textPrimary)
            Text(subtitle)
                .appFont(14)
                .foregroundColor(Theme.textSecondary)
        }
        .multilineTextAlignment(.center)
        .frame(maxWidth: .infinity)
        .padding(.bottom, 18)
    }
}
