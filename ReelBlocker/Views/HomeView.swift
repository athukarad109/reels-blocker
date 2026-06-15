import SwiftUI

struct HomeView: View {
    @EnvironmentObject private var screenTimeManager: ScreenTimeManager
    @EnvironmentObject private var sessionManager: SessionManager
    @State private var showSettings = false
    @State private var showUnlockPrompt = false
    @State private var errorMessage: String?
    @State private var isStartingSession = false
    @State private var now = Date()

    private let timer = Timer.publish(every: 1, on: .main, in: .common).autoconnect()

    var body: some View {
        NavigationStack {
            VStack(spacing: 24) {
                statusCard

                if sessionManager.isSessionActive {
                    sessionActiveContent
                } else {
                    sessionInactiveContent
                }

                if let errorMessage {
                    Text(errorMessage)
                        .font(.footnote)
                        .foregroundStyle(.red)
                        .multilineTextAlignment(.center)
                }

                tipCard

                Spacer()
            }
            .padding()
            .navigationTitle("Reel Blocker")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Settings", systemImage: "gearshape") {
                        showSettings = true
                    }
                }
            }
            .sheet(isPresented: $showSettings) {
                SettingsView()
            }
            .alert("Unlock requested", isPresented: $showUnlockPrompt) {
                Button("Start create session") {
                    Task { await startCreateSession() }
                }
                Button("Cancel", role: .cancel) {}
            } message: {
                Text("You tried to open Instagram while it was locked. Start a timed create session to continue.")
            }
            .onAppear {
                sessionManager.syncFromStorage()
                sessionManager.checkExpiredSession()
                screenTimeManager.enforceShieldState()
                checkUnlockRequest()
            }
            .onReceive(timer) { date in
                now = date
                sessionManager.checkExpiredSession()
            }
        }
    }

    private var statusCard: some View {
        VStack(spacing: 8) {
            Image(systemName: sessionManager.isSessionActive ? "lock.open.fill" : "lock.fill")
                .font(.system(size: 44))
                .foregroundStyle(sessionManager.isSessionActive ? .green : .orange)

            Text(sessionManager.isSessionActive ? "Create session active" : "Instagram locked")
                .font(.title3.bold())

            if sessionManager.isSessionActive, let remaining = sessionManager.remainingTimeInterval {
                Text("Unlocked until \(formattedExpiry)")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                Text(countdownText(remaining))
                    .font(.title2.monospacedDigit())
            } else {
                Text("Open a timed session to post on Instagram")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    private var sessionActiveContent: some View {
        VStack(spacing: 12) {
            Button("Open Create in Instagram", systemImage: "camera.fill") {
                openInstagramCreate()
            }
            .buttonStyle(.borderedProminent)
            .frame(maxWidth: .infinity)

            Button("End Session", systemImage: "lock.fill", role: .destructive) {
                sessionManager.endSession()
            }
            .buttonStyle(.bordered)
            .frame(maxWidth: .infinity)
        }
    }

    private var sessionInactiveContent: some View {
        Button {
            Task { await startCreateSession() }
        } label: {
            HStack {
                if isStartingSession {
                    ProgressView()
                }
                Label("Create Post", systemImage: "plus.circle.fill")
            }
            .frame(maxWidth: .infinity)
        }
        .buttonStyle(.borderedProminent)
        .disabled(isStartingSession || !AppGroupStorage.shared.blockerEnabled)
    }

    private var tipCard: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Tip")
                .font(.headline)
            Text("Remove Instagram from your home screen and use Reel Blocker as your entry point. Reels and Explore remain visible during active sessions—use shorter durations in Settings if needed.")
                .font(.footnote)
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(Color(.tertiarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }

    private var formattedExpiry: String {
        guard let expiresAt = sessionManager.sessionExpiresAt else { return "" }
        return expiresAt.formatted(date: .omitted, time: .shortened)
    }

    private func countdownText(_ interval: TimeInterval) -> String {
        let total = Int(interval)
        let minutes = total / 60
        let seconds = total % 60
        return String(format: "%02d:%02d remaining", minutes, seconds)
    }

    private func checkUnlockRequest() {
        if sessionManager.consumeUnlockRequest() {
            showUnlockPrompt = true
        }
    }

    private func startCreateSession() async {
        isStartingSession = true
        errorMessage = nil
        defer { isStartingSession = false }

        do {
            try await sessionManager.startSession()
            openInstagramCreate()
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private func openInstagramCreate() {
        switch InstagramLauncher.openCreate() {
        case .opened:
            break
        case .notInstalled:
            errorMessage = "Instagram is not installed on this device."
        }
    }
}
