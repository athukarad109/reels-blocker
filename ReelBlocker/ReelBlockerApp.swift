import SwiftUI

@main
struct ReelBlockerApp: App {
    @Environment(\.scenePhase) private var scenePhase
    @StateObject private var screenTimeManager = ScreenTimeManager.shared
    @StateObject private var sessionManager = SessionManager.shared

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(screenTimeManager)
                .environmentObject(sessionManager)
                .onChange(of: scenePhase) { _, newPhase in
                    guard newPhase == .active else { return }
                    sessionManager.syncFromStorage()
                    sessionManager.checkExpiredSession()
                    screenTimeManager.enforceShieldState()
                }
        }
    }
}

struct RootView: View {
    @EnvironmentObject private var screenTimeManager: ScreenTimeManager
    @AppStorage(AppConstants.StorageKey.hasCompletedOnboarding, store: UserDefaults(suiteName: AppConstants.appGroupID))
    private var hasCompletedOnboarding = false

    var body: some View {
        Group {
            if hasCompletedOnboarding && screenTimeManager.hasInstagramSelected {
                HomeView()
            } else {
                OnboardingView()
            }
        }
    }
}
