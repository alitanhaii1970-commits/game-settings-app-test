import SwiftUI

struct GameListView: View {
    @EnvironmentObject var state: AppState
    @State private var query = ""
    @State private var refreshSpin = 0.0
    @State private var showSettings = false
    @State private var selectedGame: Game?

    private var filtered: [Game] {
        guard !query.trimmingCharacters(in: .whitespaces).isEmpty else { return state.games }
        return state.games.filter { $0.name.localizedCaseInsensitiveContains(query) }
    }

    var body: some View {
        ZStack {
            Theme.background.ignoresSafeArea()

            VStack(spacing: 0) {
                header
                searchField

                ZStack {
                    if state.isLoading && state.games.isEmpty {
                        ProgressView().tint(Theme.red)
                    } else if let message = state.emptyMessage, state.games.isEmpty {
                        emptyState(message)
                    } else if filtered.isEmpty && !state.games.isEmpty {
                        emptyState(state.t("no_results"))
                    } else {
                        list
                    }
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            }

            if let toast = state.toastMessage {
                VStack {
                    Spacer()
                    ToastView(text: toast)
                        .padding(.bottom, 24)
                }
                .transition(.move(edge: .bottom).combined(with: .opacity))
                .animation(.spring(response: 0.4, dampingFraction: 0.8), value: state.toastMessage)
            }
        }
        .navigationBarHidden(true)
        .navigationDestination(item: $selectedGame) { game in
            GameDetailView(game: game)
        }
        .fullScreenCover(isPresented: $showSettings) {
            SettingsView()
        }
        .task { await state.loadInitial() }
    }

    private var header: some View {
        HStack(spacing: 10) {
            Image("Logo")
                .resizable()
                .aspectRatio(contentMode: .fill)
                .frame(width: 34, height: 34)
                .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))

            Text("PC Max Test")
                .appFont(22, bold: true, style: .title2)
                .foregroundColor(Theme.textPrimary)

            Spacer(minLength: 8)

            RoundIconButton(systemName: "gearshape.fill", tint: Theme.red) {
                showSettings = true
            }

            RoundIconButton(systemName: "arrow.clockwise", rotation: refreshSpin, dimmed: state.isLoading, tint: Theme.red) {
                guard !state.isLoading else { return }
                withAnimation(.easeOut(duration: 0.5)) { refreshSpin += 360 }
                Task { await state.refresh(forceServer: true) }
            }
        }
        .padding(.horizontal, 16)
        .padding(.top, 8)
        .padding(.bottom, 12)
    }

    private var searchField: some View {
        HStack(spacing: 10) {
            Image(systemName: "magnifyingglass")
                .foregroundColor(Theme.textSecondary)
                .font(.system(size: 15))
            TextField("", text: $query, prompt: Text(state.t("search_hint")).foregroundColor(Theme.textSecondary))
                .appFont(15)
                .foregroundColor(Theme.textPrimary)
                .autocorrectionDisabled()
                .textInputAutocapitalization(.never)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(Theme.surface)
        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
        .overlay(RoundedRectangle(cornerRadius: 16, style: .continuous).stroke(Theme.border, lineWidth: 1))
        .padding(.horizontal, 16)
        .padding(.bottom, 12)
    }

    private var list: some View {
        ScrollView {
            LazyVStack(spacing: 10) {
                ForEach(filtered) { game in
                    Button {
                        Haptics.light()
                        selectedGame = game
                    } label: {
                        GameRow(game: game)
                    }
                    .buttonStyle(PressableStyle())
                }
            }
            .padding(.horizontal, 16)
            .padding(.bottom, 16)
        }
        .refreshable {
            await state.refresh(forceServer: true)
        }
    }

    private func emptyState(_ text: String) -> some View {
        Text(text)
            .appFont(15)
            .foregroundColor(Theme.textSecondary)
            .multilineTextAlignment(.center)
            .padding(32)
    }
}

private struct GameRow: View {
    let game: Game

    var body: some View {
        HStack(spacing: 14) {
            RemoteImage(url: game.imageUrl, maxPixel: 160)
                .frame(width: 56, height: 56)
                .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))

            Text(game.name)
                .appFont(16, bold: true)
                .foregroundColor(Theme.textPrimary)
                .lineLimit(1)

            Spacer(minLength: 8)

            Image(systemName: "chevron.forward")
                .font(.system(size: 13, weight: .semibold))
                .foregroundColor(Theme.textSecondary)
        }
        .padding(12)
        .background(Theme.surface)
        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
        .overlay(RoundedRectangle(cornerRadius: 14, style: .continuous).stroke(Theme.border, lineWidth: 1))
    }
}

private struct ToastView: View {
    let text: String

    var body: some View {
        Text(text)
            .appFont(14, bold: true)
            .foregroundColor(Theme.textPrimary)
            .padding(.horizontal, 18)
            .padding(.vertical, 12)
            .background(Theme.surfaceAlt)
            .clipShape(Capsule())
            .overlay(Capsule().stroke(Theme.border, lineWidth: 1))
            .shadow(color: .black.opacity(0.4), radius: 12, x: 0, y: 4)
    }
}
