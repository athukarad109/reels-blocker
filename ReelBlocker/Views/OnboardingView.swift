import FamilyControls
import SwiftUI

struct OnboardingView: View {
    @EnvironmentObject private var screenTimeManager: ScreenTimeManager
    @AppStorage(AppConstants.StorageKey.hasCompletedOnboarding, store: UserDefaults(suiteName: AppConstants.appGroupID))
    private var hasCompletedOnboarding = false
    @State private var isPickerPresented = false
    @State private var isRequestingAuthorization = false
    @State private var errorMessage: String?

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    header

                    VStack(alignment: .leading, spacing: 12) {
                        Label("Blocks Instagram by default", systemImage: "lock.shield")
                        Label("Unlocks only for timed create sessions", systemImage: "camera")
                        Label("Re-locks automatically when time ends", systemImage: "clock.badge.checkmark")
                    }
                    .font(.subheadline)
                    .foregroundStyle(.secondary)

                    limitationCard

                    if screenTimeManager.authorizationStatus != .approved {
                        Button {
                            Task { await requestAuthorization() }
                        } label: {
                            HStack {
                                if isRequestingAuthorization {
                                    ProgressView()
                                }
                                Text("Allow Screen Time Access")
                            }
                            .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.borderedProminent)
                        .disabled(isRequestingAuthorization)
                    }

                    if screenTimeManager.authorizationStatus == .approved {
                        Button("Select Instagram") {
                            isPickerPresented = true
                        }
                        .buttonStyle(.borderedProminent)
                        .frame(maxWidth: .infinity)

                        if screenTimeManager.hasInstagramSelected {
                            Button("Finish Setup") {
                                completeOnboarding()
                            }
                            .buttonStyle(.bordered)
                            .frame(maxWidth: .infinity)
                        }
                    }

                    if let errorMessage {
                        Text(errorMessage)
                            .font(.footnote)
                            .foregroundStyle(.red)
                    }
                }
                .padding()
            }
            .navigationTitle("Reel Blocker")
            .familyActivityPicker(isPresented: $isPickerPresented, selection: $screenTimeManager.instagramSelection)
            .onChange(of: screenTimeManager.instagramSelection) { _, newValue in
                screenTimeManager.saveInstagramSelection(newValue)
            }
        }
    }

    private var header: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Gatekeeper for Instagram")
                .font(.title2.bold())
            Text("Instagram stays locked unless you start a create session from this app.")
                .foregroundStyle(.secondary)
        }
    }

    private var limitationCard: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Important")
                .font(.headline)
            Text("iOS cannot hide Reels or Explore tabs inside Instagram. This app blocks access and opens the create flow via instagram://camera. During an active session, shorter durations reduce time spent browsing.")
                .font(.footnote)
                .foregroundStyle(.secondary)
        }
        .padding()
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }

    private func requestAuthorization() async {
        isRequestingAuthorization = true
        errorMessage = nil
        defer { isRequestingAuthorization = false }

        do {
            try await screenTimeManager.requestAuthorization()
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private func completeOnboarding() {
        hasCompletedOnboarding = true
        AppGroupStorage.shared.blockerEnabled = true
        screenTimeManager.setBlockerEnabled(true)
    }
}
