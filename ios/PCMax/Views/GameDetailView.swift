import SwiftUI
import UIKit

struct GameDetailView: View {
    @EnvironmentObject var state: AppState
    @Environment(\.dismiss) private var dismiss
    let game: Game

    @State private var imagePressed = false
    @State private var showPreview = false
    @State private var pulse = false

    /// همان منطق GameDetailActivity: فقط اگر تنظیمات قدرت سیستم قبلاً انتخاب شده،
    /// بخشی که مناسب‌تره (سبز برای ضعیف/متوسط، زرد برای قوی) با حاشیه‌ی رنگی و
    /// یک نفسِ ملایم مشخص می‌شه — فقط اگر آن بخش واقعاً محتوا داشته باشد.
    private var recommendGreen: Bool { state.tier != AppState.tierStrong }

    var body: some View {
        ZStack(alignment: .top) {
            Theme.background.ignoresSafeArea()

            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    image
                        .padding(.horizontal, 16)
                        .padding(.top, 16)

                    if game.hasYoutubeMode {
                        youtubeCard
                            .padding(.horizontal, 16)
                            .padding(.top, 24)
                    } else {
                        settingsSections
                            .padding(.horizontal, 16)
                            .padding(.top, 20)
                    }
                }
                .padding(.bottom, 24)
            }
            .padding(.top, 56)

            TopBar(title: game.name, onBack: { dismiss() })
                .background(Theme.background)
        }
        .navigationBarHidden(true)
        .fullScreenCover(isPresented: $showPreview) {
            ImagePreviewView(url: game.imageUrl)
        }
        .onAppear {
            withAnimation(.easeInOut(duration: 1.6).repeatForever(autoreverses: true)) {
                pulse = !UIAccessibility.isReduceMotionEnabled
            }
        }
    }

    private var image: some View {
        RemoteImage(url: game.imageUrl, maxPixel: 900, contentMode: .fill)
            .frame(height: 200)
            .frame(maxWidth: .infinity)
            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
            .scaleEffect(imagePressed ? 0.98 : 1)
            .overlay(alignment: .bottomTrailing) {
                Image(systemName: "arrow.up.left.and.arrow.down.right")
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(.white)
                    .padding(8)
                    .background(Color.black.opacity(0.45))
                    .clipShape(Circle())
                    .padding(10)
            }
            .contentShape(Rectangle())
            .onTapGesture {
                Haptics.light()
                showPreview = true
            }
            .simultaneousGesture(
                DragGesture(minimumDistance: 0)
                    .onChanged { _ in imagePressed = true }
                    .onEnded { _ in
                        withAnimation(.interpolatingSpring(stiffness: 300, damping: 12)) {
                            imagePressed = false
                        }
                    }
            )
            .animation(.easeOut(duration: 0.1), value: imagePressed)
    }

    private var youtubeCard: some View {
        VStack(spacing: 16) {
            Image(systemName: "play.rectangle.fill")
                .font(.system(size: 46))
                .foregroundColor(Theme.red)

            Text(state.t("youtube_hint"))
                .appFont(15)
                .foregroundColor(Theme.textSecondary)
                .multilineTextAlignment(.center)
                .lineSpacing(4)

            Button {
                Haptics.light()
                if let url = URL(string: game.youtubeUrl.trimmingCharacters(in: .whitespacesAndNewlines)) {
                    UIApplication.shared.open(url)
                }
            } label: {
                Label(state.t("watch_on_youtube"), systemImage: "play.fill")
                    .appFont(15, bold: true)
            }
            .buttonStyle(PrimaryButtonStyle())
            .frame(maxWidth: 260)
        }
        .frame(maxWidth: .infinity)
        .padding(28)
        .background(Theme.surface)
        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
        .overlay(RoundedRectangle(cornerRadius: 16, style: .continuous).stroke(Theme.border, lineWidth: 1))
    }

    @ViewBuilder
    private var settingsSections: some View {
        let hasGreen = game.hasGreen
        let hasYellow = game.hasYellow
        let showFallback = !hasGreen && !hasYellow

        VStack(alignment: .leading, spacing: 16) {
            if hasGreen || showFallback {
                SettingsBlock(
                    tag: state.t("green_settings"),
                    tagColor: Theme.green,
                    tagBG: Theme.greenBG,
                    text: showFallback ? state.t("no_settings") : game.settingsGreen,
                    glow: !showFallback && recommendGreen && !state.tier.isEmpty,
                    pulse: pulse
                )
            }
            if hasYellow {
                SettingsBlock(
                    tag: state.t("yellow_settings"),
                    tagColor: Theme.yellow,
                    tagBG: Theme.yellowBG,
                    text: game.settingsYellow,
                    glow: !recommendGreen && !state.tier.isEmpty,
                    pulse: pulse
                )
            }
        }
    }
}

private struct SettingsBlock: View {
    @EnvironmentObject var state: AppState
    let tag: String
    let tagColor: Color
    let tagBG: Color
    let text: String
    let glow: Bool
    let pulse: Bool

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text(tag)
                .appFont(13, bold: true)
                .foregroundColor(tagColor)
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(tagBG)
                .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
                .overlay(RoundedRectangle(cornerRadius: 8, style: .continuous).stroke(tagColor, lineWidth: 1))

            Text(text)
                .appFont(15)
                .foregroundColor(Theme.textPrimary)
                .lineSpacing(6)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(16)
                .background(Theme.surface)
                .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                .overlay(
                    RoundedRectangle(cornerRadius: 14, style: .continuous)
                        .stroke(glow ? tagColor : Theme.border, lineWidth: glow ? 1.5 : 1)
                )
                .scaleEffect(glow && pulse ? 1.018 : 1)
        }
    }
}
