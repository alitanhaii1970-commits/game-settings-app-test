import SwiftUI

@main
struct PCMaxApp: App {
    @StateObject private var state = AppState()

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(state)
                .environment(\.layoutDirection, state.lang == .fa ? .rightToLeft : .leftToRight)
                .preferredColorScheme(.dark)
                .tint(Theme.red)
        }
    }
}

private struct RootView: View {
    @EnvironmentObject var state: AppState

    var body: some View {
        Group {
            if state.onboardingDone {
                NavigationStack {
                    GameListView()
                }
            } else {
                OnboardingView()
            }
        }
        .animation(.easeInOut(duration: 0.3), value: state.onboardingDone)
    }
}
