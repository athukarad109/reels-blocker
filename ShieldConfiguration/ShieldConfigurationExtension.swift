import ManagedSettings
import ManagedSettingsUI
import UIKit

class ShieldConfigurationExtension: ShieldConfigurationDataSource {
    override func configuration(shielding application: Application) -> ShieldConfiguration {
        makeConfiguration()
    }

    override func configuration(shielding application: Application, in category: ActivityCategory) -> ShieldConfiguration {
        makeConfiguration()
    }

    private func makeConfiguration() -> ShieldConfiguration {
        ShieldConfiguration(
            backgroundBlurStyle: .systemThickMaterial,
            backgroundColor: UIColor.systemBackground,
            icon: UIImage(systemName: "lock.fill"),
            title: ShieldConfiguration.Label(text: "Instagram is locked", color: .label),
            subtitle: ShieldConfiguration.Label(
                text: "Use Reel Blocker to start a create session",
                color: .secondaryLabel
            ),
            primaryButtonLabel: ShieldConfiguration.Label(text: "OK", color: .label),
            secondaryButtonLabel: ShieldConfiguration.Label(text: "Request unlock", color: .systemBlue)
        )
    }
}
