import UIKit

enum InstagramLauncher {
    private static let createURL = URL(string: "instagram://camera")!
    private static let appStoreURL = URL(string: "https://apps.apple.com/app/instagram/id389801252")!

    static var isInstalled: Bool {
        UIApplication.shared.canOpenURL(createURL)
    }

    @MainActor
    static func openCreate() -> InstagramLaunchResult {
        guard isInstalled else {
            return .notInstalled
        }

        UIApplication.shared.open(createURL, options: [:], completionHandler: nil)
        return .opened
    }
}

enum InstagramLaunchResult {
    case opened
    case notInstalled
}
