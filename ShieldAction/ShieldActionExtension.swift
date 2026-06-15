import ManagedSettings

class ShieldActionExtension: ShieldActionDelegate {
    override func handle(
        action: ShieldAction,
        for application: Application,
        completionHandler: @escaping (ShieldActionResponse) -> Void
    ) {
        switch action {
        case .primaryButtonPressed:
            completionHandler(.close)
        case .secondaryButtonPressed:
            AppGroupStorage.shared.unlockRequested = true
            completionHandler(.defer)
        @unknown default:
            completionHandler(.close)
        }
    }

    override func handle(
        action: ShieldAction,
        for application: Application,
        in category: ActivityCategory,
        completionHandler: @escaping (ShieldActionResponse) -> Void
    ) {
        handle(action: action, for: application, completionHandler: completionHandler)
    }
}
