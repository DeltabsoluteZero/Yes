package com.pixelsu.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    var autoStartOnBoot: Boolean = false,
    var showNotifications: Boolean = true,
    var persistentNotification: Boolean = true,
    var biometricLockEnabled: Boolean = false,
    var theme: ThemeMode = ThemeMode.SYSTEM,
    var language: String = "en",
    var logLevel: LogLevel = LogLevel.INFO,
    var magiskBinaryPath: String = "/sbin/magisk",
    var preferredShell: ShellType = ShellType.SU,
    var selinuxMode: SELinuxMode = SELinuxMode.PERMISSIVE,
    var zygiskEnabled: Boolean = false,
    var denyListMode: Boolean = true,
    var defaultTempRootDuration: Int = 60,
    var autoDisableTempRoot: Boolean = true,
    var notifyTempRootExpiry: Boolean = true,
    var defaultHideMode: HideMode = HideMode.NONE,
    var hideListConfig: HideListConfig = HideListConfig(),
    var autoUpdateExploits: Boolean = false,
    var exploitDownloadPath: String = "",
    var verifyChecksums: Boolean = true,
    var allowIncompatibleExploits: Boolean = false,
    var enableDebugLogging: Boolean = false,
    var keepScreenOn: Boolean = false,
    var vibrationEnabled: Boolean = true,
    var soundEnabled: Boolean = false,
    var autoClearLogsDays: Int = 30,
    var maxLogEntries: Int = 1000,
    var requireAuthForRoot: Boolean = true,
    var requireAuthForExploits: Boolean = true,
    var requireAuthForHide: Boolean = true,
    var requireAuthForSettings: Boolean = false,
    var lastUpdated: Long = System.currentTimeMillis(),
    var version: Int = 1
) : Parcelable {

    constructor(parcel: Parcel) : this(
        autoStartOnBoot = parcel.readInt() != 0,
        showNotifications = parcel.readInt() != 0,
        persistentNotification = parcel.readInt() != 0,
        biometricLockEnabled = parcel.readInt() != 0,
        theme = ThemeMode.valueOf(parcel.readString()!!),
        language = parcel.readString()!!,
        logLevel = LogLevel.valueOf(parcel.readString()!!),
        magiskBinaryPath = parcel.readString()!!,
        preferredShell = ShellType.valueOf(parcel.readString()!!),
        selinuxMode = SELinuxMode.valueOf(parcel.readString()!!),
        zygiskEnabled = parcel.readInt() != 0,
        denyListMode = parcel.readInt() != 0,
        defaultTempRootDuration = parcel.readInt(),
        autoDisableTempRoot = parcel.readInt() != 0,
        notifyTempRootExpiry = parcel.readInt() != 0,
        defaultHideMode = HideMode.valueOf(parcel.readString()!!),
        hideListConfig = parcel.readParcelable(HideListConfig::class.java.classLoader)!!,
        autoUpdateExploits = parcel.readInt() != 0,
        exploitDownloadPath = parcel.readString()!!,
        verifyChecksums = parcel.readInt() != 0,
        allowIncompatibleExploits = parcel.readInt() != 0,
        enableDebugLogging = parcel.readInt() != 0,
        keepScreenOn = parcel.readInt() != 0,
        vibrationEnabled = parcel.readInt() != 0,
        soundEnabled = parcel.readInt() != 0,
        autoClearLogsDays = parcel.readInt(),
        maxLogEntries = parcel.readInt(),
        requireAuthForRoot = parcel.readInt() != 0,
        requireAuthForExploits = parcel.readInt() != 0,
        requireAuthForHide = parcel.readInt() != 0,
        requireAuthForSettings = parcel.readInt() != 0,
        lastUpdated = parcel.readLong(),
        version = parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(if (autoStartOnBoot) 1 else 0)
        parcel.writeInt(if (showNotifications) 1 else 0)
        parcel.writeInt(if (persistentNotification) 1 else 0)
        parcel.writeInt(if (biometricLockEnabled) 1 else 0)
        parcel.writeString(theme.name)
        parcel.writeString(language)
        parcel.writeString(logLevel.name)
        parcel.writeString(magiskBinaryPath)
        parcel.writeString(preferredShell.name)
        parcel.writeString(selinuxMode.name)
        parcel.writeInt(if (zygiskEnabled) 1 else 0)
        parcel.writeInt(if (denyListMode) 1 else 0)
        parcel.writeInt(defaultTempRootDuration)
        parcel.writeInt(if (autoDisableTempRoot) 1 else 0)
        parcel.writeInt(if (notifyTempRootExpiry) 1 else 0)
        parcel.writeString(defaultHideMode.name)
        parcel.writeParcelable(hideListConfig, flags)
        parcel.writeInt(if (autoUpdateExploits) 1 else 0)
        parcel.writeString(exploitDownloadPath)
        parcel.writeInt(if (verifyChecksums) 1 else 0)
        parcel.writeInt(if (allowIncompatibleExploits) 1 else 0)
        parcel.writeInt(if (enableDebugLogging) 1 else 0)
        parcel.writeInt(if (keepScreenOn) 1 else 0)
        parcel.writeInt(if (vibrationEnabled) 1 else 0)
        parcel.writeInt(if (soundEnabled) 1 else 0)
        parcel.writeInt(autoClearLogsDays)
        parcel.writeInt(maxLogEntries)
        parcel.writeInt(if (requireAuthForRoot) 1 else 0)
        parcel.writeInt(if (requireAuthForExploits) 1 else 0)
        parcel.writeInt(if (requireAuthForHide) 1 else 0)
        parcel.writeInt(if (requireAuthForSettings) 1 else 0)
        parcel.writeLong(lastUpdated)
        parcel.writeInt(version)
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<AppSettings> = object : Parcelable.Creator<AppSettings> {
            override fun createFromParcel(parcel: Parcel): AppSettings = AppSettings(parcel)
            override fun newArray(size: Int): Array<AppSettings?> = arrayOfNulls(size)
        }

        fun getDefaults(): AppSettings = AppSettings()
    }
}
