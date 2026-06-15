import FamilyControls
import Foundation

@MainActor
final class ScreenTimeManager: ObservableObject {
    static let shared = ScreenTimeManager()

    @Published var authorizationStatus: AuthorizationStatus = .notDetermined
    @Published var instagramSelection = FamilyActivitySelection()
    @Published var hasInstagramSelected: Bool = false

    private let storage = AppGroupStorage.shared

    private init() {
        authorizationStatus = AuthorizationCenter.shared.authorizationStatus
        if let saved = storage.loadInstagramSelection() {
            instagramSelection = saved
            hasInstagramSelected = !saved.applicationTokens.isEmpty
        }
    }

    func refreshAuthorizationStatus() {
        authorizationStatus = AuthorizationCenter.shared.authorizationStatus
    }

    func requestAuthorization() async throws {
        try await AuthorizationCenter.shared.requestAuthorization(for: .individual)
        refreshAuthorizationStatus()
    }

    func saveInstagramSelection(_ selection: FamilyActivitySelection) {
        instagramSelection = selection
        hasInstagramSelected = !selection.applicationTokens.isEmpty
        storage.saveInstagramSelection(selection)

        if storage.blockerEnabled && !storage.sessionActive {
            ShieldManager.applyShield()
        }
    }

    func setBlockerEnabled(_ enabled: Bool) {
        storage.blockerEnabled = enabled
        ShieldManager.setShieldEnabled(enabled && !storage.sessionActive)
    }

    func unshieldInstagram() {
        ShieldManager.removeShield()
    }

    func reshieldInstagram() {
        ShieldManager.endSessionAndReshield()
    }

    func enforceShieldState() {
        ShieldManager.reshieldIfNeeded()
    }
}
