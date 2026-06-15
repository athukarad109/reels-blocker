import FamilyControls
import SwiftUI

struct SettingsView: View {
    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject private var screenTimeManager: ScreenTimeManager
    @State private var blockerEnabled = AppGroupStorage.shared.blockerEnabled
    @State private var sessionDuration = AppGroupStorage.shared.sessionDurationMinutes
    @State private var isPickerPresented = false

    var body: some View {
        NavigationStack {
            Form {
                Section("Blocker") {
                    Toggle("Block Instagram", isOn: $blockerEnabled)
                        .onChange(of: blockerEnabled) { _, newValue in
                            screenTimeManager.setBlockerEnabled(newValue)
                        }
                }

                Section("Create session") {
                    Picker("Session duration", selection: $sessionDuration) {
                        ForEach(AppConstants.sessionDurationOptions, id: \.self) { minutes in
                            Text("\(minutes) minutes").tag(minutes)
                        }
                    }
                    .onChange(of: sessionDuration) { _, newValue in
                        AppGroupStorage.shared.sessionDurationMinutes = newValue
                    }
                }

                Section("Instagram app") {
                    Button("Re-select Instagram") {
                        isPickerPresented = true
                    }
                }

                Section("About") {
                    Text("Reel Blocker uses Apple's Screen Time API to shield Instagram and unlock it only for timed create sessions.")
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                }
            }
            .navigationTitle("Settings")
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Done") { dismiss() }
                }
            }
            .familyActivityPicker(isPresented: $isPickerPresented, selection: $screenTimeManager.instagramSelection)
            .onChange(of: screenTimeManager.instagramSelection) { _, newValue in
                screenTimeManager.saveInstagramSelection(newValue)
            }
        }
    }
}
