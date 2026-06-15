import DeviceActivity
import Foundation

enum AppConstants {
    static let appGroupID = "group.com.reelblocker.shared"
    static let mainBundleID = "com.reelblocker.app"
    static let createSessionActivity = DeviceActivityName("createSession")

    enum StorageKey {
        static let instagramSelection = "instagramSelection"
        static let sessionActive = "sessionActive"
        static let sessionExpiresAt = "sessionExpiresAt"
        static let unlockRequested = "unlockRequested"
        static let blockerEnabled = "blockerEnabled"
        static let sessionDurationMinutes = "sessionDurationMinutes"
        static let hasCompletedOnboarding = "hasCompletedOnboarding"
    }

    static let defaultSessionDurationMinutes = 10
    static let sessionDurationOptions = [3, 5, 10, 15]
}
