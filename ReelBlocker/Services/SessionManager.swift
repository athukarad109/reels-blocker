import DeviceActivity
import Foundation
import UserNotifications

@MainActor
final class SessionManager: ObservableObject {
    static let shared = SessionManager()

    @Published private(set) var isSessionActive = false
    @Published private(set) var sessionExpiresAt: Date?

    private let storage = AppGroupStorage.shared
    private let activityCenter = DeviceActivityCenter()
    private let screenTimeManager = ScreenTimeManager.shared

    private init() {
        syncFromStorage()
    }

    func syncFromStorage() {
        isSessionActive = storage.sessionActive
        sessionExpiresAt = storage.sessionExpiresAt
    }

    var remainingTimeInterval: TimeInterval? {
        guard let expiresAt = sessionExpiresAt else { return nil }
        return max(0, expiresAt.timeIntervalSinceNow)
    }

    func startSession() async throws {
        guard screenTimeManager.hasInstagramSelected else {
            throw SessionError.instagramNotSelected
        }

        let durationMinutes = storage.sessionDurationMinutes
        let expiresAt = Calendar.current.date(byAdding: .minute, value: durationMinutes, to: Date()) ?? Date()

        storage.sessionActive = true
        storage.sessionExpiresAt = expiresAt
        storage.unlockRequested = false
        syncFromStorage()

        screenTimeManager.unshieldInstagram()
        try startDeviceActivityMonitoring(until: expiresAt)
        await scheduleSessionNotifications(expiresAt: expiresAt)
    }

    func endSession() {
        stopDeviceActivityMonitoring()
        screenTimeManager.reshieldInstagram()
        syncFromStorage()
        UNUserNotificationCenter.current().removePendingNotificationRequests(withIdentifiers: sessionNotificationIDs)
    }

    func checkExpiredSession() {
        guard storage.sessionActive else { return }
        guard let expiresAt = storage.sessionExpiresAt, Date() >= expiresAt else { return }
        endSession()
    }

    func consumeUnlockRequest() -> Bool {
        guard storage.unlockRequested else { return false }
        storage.unlockRequested = false
        return true
    }

    private func startDeviceActivityMonitoring(until endDate: Date) throws {
        let calendar = Calendar.current
        let now = Date()
        let schedule = DeviceActivitySchedule(
            intervalStart: calendar.dateComponents([.hour, .minute, .second], from: now),
            intervalEnd: calendar.dateComponents([.hour, .minute, .second], from: endDate),
            repeats: false
        )

        activityCenter.stopMonitoring([AppConstants.createSessionActivity])
        try activityCenter.startMonitoring(AppConstants.createSessionActivity, during: schedule)
    }

    private func stopDeviceActivityMonitoring() {
        activityCenter.stopMonitoring([AppConstants.createSessionActivity])
    }

    private var sessionNotificationIDs: [String] {
        ["session-ending-soon", "session-ended"]
    }

    private func scheduleSessionNotifications(expiresAt: Date) async {
        let center = UNUserNotificationCenter.current()
        _ = try? await center.requestAuthorization(options: [.alert, .sound])

        center.removePendingNotificationRequests(withIdentifiers: sessionNotificationIDs)

        let warningDate = expiresAt.addingTimeInterval(-60)
        if warningDate > Date() {
            let warningContent = UNMutableNotificationContent()
            warningContent.title = "Session ending soon"
            warningContent.body = "Instagram will lock in 1 minute. Tap End Session in Reel Blocker when done."
            warningContent.sound = .default

            let warningTrigger = UNCalendarNotificationTrigger(
                dateMatching: Calendar.current.dateComponents([.year, .month, .day, .hour, .minute, .second], from: warningDate),
                repeats: false
            )
            center.add(UNNotificationRequest(identifier: "session-ending-soon", content: warningContent, trigger: warningTrigger))
        }

        let endContent = UNMutableNotificationContent()
        endContent.title = "Instagram locked"
        endContent.body = "Your create session has ended. Open Reel Blocker to post again."
        endContent.sound = .default

        let endTrigger = UNCalendarNotificationTrigger(
            dateMatching: Calendar.current.dateComponents([.year, .month, .day, .hour, .minute, .second], from: expiresAt),
            repeats: false
        )
        center.add(UNNotificationRequest(identifier: "session-ended", content: endContent, trigger: endTrigger))
    }
}

enum SessionError: LocalizedError {
    case instagramNotSelected

    var errorDescription: String? {
        switch self {
        case .instagramNotSelected:
            return "Select Instagram during setup before starting a session."
        }
    }
}
