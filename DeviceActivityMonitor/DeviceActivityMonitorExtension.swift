import DeviceActivity
import Foundation

class DeviceActivityMonitorExtension: DeviceActivityMonitor {
    override func intervalDidEnd(for activity: DeviceActivityName) {
        super.intervalDidEnd(for: activity)

        guard activity == AppConstants.createSessionActivity else { return }
        ShieldManager.endSessionAndReshield()
    }

    override func eventDidReachThreshold(_ event: DeviceActivityEvent.Name, activity: DeviceActivityName) {
        super.eventDidReachThreshold(event, activity: activity)

        guard activity == AppConstants.createSessionActivity else { return }
        ShieldManager.endSessionAndReshield()
    }
}
