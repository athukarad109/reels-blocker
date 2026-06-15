import FamilyControls
import Foundation
import ManagedSettings

enum ShieldManager {
    private static let store = ManagedSettingsStore()

    static func applicationTokens() -> Set<ApplicationToken> {
        AppGroupStorage.shared.loadInstagramSelection()?.applicationTokens ?? []
    }

    static func applyShield() {
        let tokens = applicationTokens()
        guard !tokens.isEmpty else { return }
        store.shield.applications = tokens
    }

    static func removeShield() {
        store.shield.applications = nil
    }

    static func setShieldEnabled(_ enabled: Bool) {
        if enabled {
            applyShield()
        } else {
            removeShield()
        }
    }

    static func reshieldIfNeeded() {
        guard AppGroupStorage.shared.blockerEnabled else {
            removeShield()
            return
        }

        if AppGroupStorage.shared.sessionActive {
            if let expiresAt = AppGroupStorage.shared.sessionExpiresAt, Date() >= expiresAt {
                endSessionAndReshield()
            }
            return
        }

        applyShield()
    }

    static func endSessionAndReshield() {
        AppGroupStorage.shared.clearSessionState()
        if AppGroupStorage.shared.blockerEnabled {
            applyShield()
        } else {
            removeShield()
        }
    }
}
