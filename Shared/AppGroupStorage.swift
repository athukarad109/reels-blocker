import FamilyControls
import Foundation

final class AppGroupStorage {
    static let shared = AppGroupStorage()

    private let defaults: UserDefaults?

    private init() {
        defaults = UserDefaults(suiteName: AppConstants.appGroupID)
    }

    var blockerEnabled: Bool {
        get { defaults?.bool(forKey: AppConstants.StorageKey.blockerEnabled) ?? true }
        set { defaults?.set(newValue, forKey: AppConstants.StorageKey.blockerEnabled) }
    }

    var sessionActive: Bool {
        get { defaults?.bool(forKey: AppConstants.StorageKey.sessionActive) ?? false }
        set { defaults?.set(newValue, forKey: AppConstants.StorageKey.sessionActive) }
    }

    var sessionExpiresAt: Date? {
        get { defaults?.object(forKey: AppConstants.StorageKey.sessionExpiresAt) as? Date }
        set { defaults?.set(newValue, forKey: AppConstants.StorageKey.sessionExpiresAt) }
    }

    var unlockRequested: Bool {
        get { defaults?.bool(forKey: AppConstants.StorageKey.unlockRequested) ?? false }
        set { defaults?.set(newValue, forKey: AppConstants.StorageKey.unlockRequested) }
    }

    var sessionDurationMinutes: Int {
        get {
            let stored = defaults?.integer(forKey: AppConstants.StorageKey.sessionDurationMinutes) ?? 0
            return stored > 0 ? stored : AppConstants.defaultSessionDurationMinutes
        }
        set { defaults?.set(newValue, forKey: AppConstants.StorageKey.sessionDurationMinutes) }
    }

    var hasCompletedOnboarding: Bool {
        get { defaults?.bool(forKey: AppConstants.StorageKey.hasCompletedOnboarding) ?? false }
        set { defaults?.set(newValue, forKey: AppConstants.StorageKey.hasCompletedOnboarding) }
    }

    func saveInstagramSelection(_ selection: FamilyActivitySelection) {
        guard let data = try? PropertyListEncoder().encode(selection) else { return }
        defaults?.set(data, forKey: AppConstants.StorageKey.instagramSelection)
    }

    func loadInstagramSelection() -> FamilyActivitySelection? {
        guard
            let data = defaults?.data(forKey: AppConstants.StorageKey.instagramSelection),
            let selection = try? PropertyListDecoder().decode(FamilyActivitySelection.self, from: data)
        else {
            return nil
        }
        return selection
    }

    func clearSessionState() {
        sessionActive = false
        sessionExpiresAt = nil
    }
}
