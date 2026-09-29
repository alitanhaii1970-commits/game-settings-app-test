import SwiftUI
import UIKit

private enum UpdateState: Equatable {
    case idle, checking, upToDate, error(String)
    case found(UpdateInfo)

    static func == (l: UpdateState, r: UpdateState) -> Bool {
        switch (l, r) {
        case (.idle, .idle), (.checking, .checking), (.upToDate, .upToDate): return true
        case (.error(let a), .error(let b)): return a == b
        case (.found(let a), .found(let b)): return a.build == b.build
        default: return false
        }
    }
}

struct SettingsView: View {
    @EnvironmentObject var state: AppState
    @Environment(\.dismiss) private var dismiss
    @State private var updateState: UpdateState = .idle

    var body: some View {
        ZStack {
            Theme.background.ignoresSafeArea()

            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    TopBar(title: state.t("settings_title"), onBack: { dismiss() })

                    sectionTitle(state.t("settings_language"))
                    RadioCard(
                        options: [(Lang.fa.rawValue, state.t("lang_fa")), (Lang.en.rawValue, state.t("lang_en"))],
                        selected: state.lang.rawValue
                    ) { id in
                        withAnimation(.easeOut(duration: 0.2)) {
                            state.lang = Lang(rawValue: id) ?? .fa
                        }
                    }
                    .padding(.horizontal, 16)

                    sectionTitle(state.t("settings_system_power"))
                    RadioCard(
                        options: [
                            (AppState.tierWeak, state.t("tier_weak")),
                            (AppState.tierMedium, state.t("tier_medium")),
                            (AppState.tierStrong, state.t("tier_strong"))
                        ],
                        selected: state.tier
                    ) { id in
                        state.tier = id
                    }
                    .padding(.horizontal, 16)

                    sectionTitle(state.t("settings_font"), color: Theme.yellow)
                    RadioCard(
                        options: AppFonts.options.map { ($0.id, $0.displayName) },
                        selected: state.fontId,
                        borderColor: Theme.yellow
                    ) { id in
                        state.fontId = id
                    }
                    .padding(.horizontal, 16)

                    about
                }
                .padding(.bottom, 24)
            }

            if updateState != .idle {
                updateOverlay
            }
        }
        .navigationBarHidden(true)
    }

    private func sectionTitle(_ text: String, color: Color = Theme.textSecondary) -> some View {
        Text(text)
            .appFont(13)
            .foregroundColor(color)
            .padding(.leading, 20)
            .padding(.top, 20)
            .padding(.bottom, 8)
    }

    private var about: some View {
        VStack(spacing: 10) {
            Image("Logo")
                .resizable()
                .aspectRatio(contentMode: .fill)
                .frame(width: 56, height: 56)
                .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))

            Text("PC Max Test")
                .appFont(14, bold: true)
                .foregroundColor(Theme.textPrimary)

            Text(String(format: state.t("version_label"), UpdateChecker.versionName))
                .appFont(12)
                .foregroundColor(Theme.textSecondary)

            Button {
                runUpdateCheck()
            } label: {
                Text(buttonLabel)
                    .appFont(13, bold: true)
                    .foregroundColor(Theme.textPrimary)
                    .padding(.horizontal, 24)
                    .padding(.vertical, 10)
                    .background(Theme.surface)
                    .clipShape(Capsule())
                    .overlay(Capsule().stroke(Theme.border, lineWidth: 1))
            }
            .buttonStyle(PressableStyle())
            .disabled(updateState == .checking)
            .padding(.top, 6)
        }
        .frame(maxWidth: .infinity)
        .padding(.top, 48)
        .padding(.bottom, 32)
    }

    private var buttonLabel: String {
        switch updateState {
        case .checking: return state.t("checking_for_update")
        case .upToDate: return state.t("up_to_date")
        case .error: return state.t("check_for_update")
        default: return state.t("check_for_update")
        }
    }

    private func runUpdateCheck() {
        updateState = .checking
        Task {
            do {
                switch try await UpdateChecker.check() {
                case .available(let info):
                    updateState = .found(info)
                case .upToDate:
                    updateState = .upToDate
                    try? await Task.sleep(nanoseconds: 2_500_000_000)
                    if updateState == .upToDate { updateState = .idle }
                }
            } catch {
                updateState = .error(error.localizedDescription)
                try? await Task.sleep(nanoseconds: 2_000_000_000)
                updateState = .idle
            }
        }
    }

    @ViewBuilder
    private var updateOverlay: some View {
        if case .found(let info) = updateState {
            ZStack {
                Color.black.opacity(0.6).ignoresSafeArea()
                    .onTapGesture { updateState = .idle }

                VStack(spacing: 16) {
                    Text(state.t("update_found_title"))
                        .appFont(18, bold: true)
                        .foregroundColor(Theme.textPrimary)

                    Text(state.t("update_ios_hint"))
                        .appFont(14)
                        .foregroundColor(Theme.textSecondary)
                        .multilineTextAlignment(.center)
                        .lineSpacing(4)

                    Button {
                        UIApplication.shared.open(info.downloadURL)
                        updateState = .idle
                    } label: {
                        Text(state.t("download"))
                            .appFont(15, bold: true)
                    }
                    .buttonStyle(PrimaryButtonStyle())

                    Button {
                        updateState = .idle
                    } label: {
                        Text(state.t("later"))
                            .appFont(14)
                            .foregroundColor(Theme.textSecondary)
                    }
                }
                .padding(24)
                .background(Theme.surface)
                .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
                .overlay(RoundedRectangle(cornerRadius: 20, style: .continuous).stroke(Theme.border, lineWidth: 1))
                .padding(.horizontal, 32)
            }
            .transition(.opacity)
        }
    }
}

/// کارت رادیویی گروهی — دقیقاً همان الگوی صفحه‌ی تنظیمات اندروید:
/// یک کارت با چند ردیف که با خط نازک از هم جدا شده‌اند.
private struct RadioCard: View {
    let options: [(id: String, label: String)]
    let selected: String
    var borderColor: Color = Theme.border
    let onSelect: (String) -> Void

    var body: some View {
        VStack(spacing: 0) {
            ForEach(Array(options.enumerated()), id: \.element.id) { index, option in
                Button {
                    Haptics.light()
                    onSelect(option.id)
                } label: {
                    HStack {
                        Text(option.label)
                            .appFont(15)
                            .foregroundColor(Theme.textPrimary)
                        Spacer()
                        ZStack {
                            Circle().stroke(option.id == selected ? Theme.red : Theme.border, lineWidth: 2)
                                .frame(width: 20, height: 20)
                            if option.id == selected {
                                Circle().fill(Theme.red).frame(width: 11, height: 11)
                            }
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 14)
                    .contentShape(Rectangle())
                }
                .buttonStyle(.plain)

                if index < options.count - 1 {
                    Divider().background(Theme.border).padding(.leading, 16)
                }
            }
        }
        .background(Theme.surface)
        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
        .overlay(RoundedRectangle(cornerRadius: 16, style: .continuous).stroke(borderColor, lineWidth: borderColor == Theme.border ? 1 : 1.5))
    }
}
