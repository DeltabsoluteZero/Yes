package com.pixelsu.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.serialization.Serializable

@Serializable
class AppInfo(
    val packageName: String = "",
    val appName: String = "",
    val versionName: String = "",
    val versionCode: Int = 0,
    val iconPath: String = "",
    val isSystemApp: Boolean = false,
    val isRootApp: Boolean = false,
    val hasRootPermissions: Boolean = false,
    val uid: Int = 0,
    val permissions: List<String> = emptyList(),
    val firstInstallTime: Long = 0,
    val lastUpdateTime: Long = 0,
    val dataDir: String = "",
    val apkPath: String = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
        packageName = parcel.readString() ?? "",
        appName = parcel.readString() ?? "",
        versionName = parcel.readString() ?? "",
        versionCode = parcel.readInt(),
        iconPath = parcel.readString() ?? "",
        isSystemApp = parcel.readInt() != 0,
        isRootApp = parcel.readInt() != 0,
        hasRootPermissions = parcel.readInt() != 0,
        uid = parcel.readInt(),
        permissions = parcel.createStringArrayList() ?: emptyList(),
        firstInstallTime = parcel.readLong(),
        lastUpdateTime = parcel.readLong(),
        dataDir = parcel.readString() ?? "",
        apkPath = parcel.readString() ?? ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(packageName)
        parcel.writeString(appName)
        parcel.writeString(versionName)
        parcel.writeInt(versionCode)
        parcel.writeString(iconPath)
        parcel.writeInt(if (isSystemApp) 1 else 0)
        parcel.writeInt(if (isRootApp) 1 else 0)
        parcel.writeInt(if (hasRootPermissions) 1 else 0)
        parcel.writeInt(uid)
        parcel.writeStringList(permissions)
        parcel.writeLong(firstInstallTime)
        parcel.writeLong(lastUpdateTime)
        parcel.writeString(dataDir)
        parcel.writeString(apkPath)
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<AppInfo> = object : Parcelable.Creator<AppInfo> {
            override fun createFromParcel(parcel: Parcel): AppInfo = AppInfo(parcel)
            override fun newArray(size: Int): Array<AppInfo?> = arrayOfNulls(size)
        }

        fun fromPackageInfo(pkg: android.content.pm.PackageInfo, label: String): AppInfo {
            val appInfo = pkg.applicationInfo
            return AppInfo(
                packageName = pkg.packageName,
                appName = label,
                versionName = pkg.versionName ?: "",
                versionCode = pkg.versionCode,
                iconPath = "",
                isSystemApp = appInfo?.let { (it.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0 } ?: false,
                isRootApp = false,
                hasRootPermissions = false,
                uid = appInfo?.uid ?: 0,
                permissions = pkg.requestedPermissions?.toList() ?: emptyList(),
                firstInstallTime = pkg.firstInstallTime,
                lastUpdateTime = pkg.lastUpdateTime,
                dataDir = appInfo?.dataDir ?: "",
                apkPath = appInfo?.sourceDir ?: ""
            )
        }
    }
}

@Serializable
class HideRule(
    val packageName: String = "",
    val hideRoot: Boolean = true,
    val hideSuBinary: Boolean = true,
    val hideMagiskFiles: Boolean = true,
    val hideProps: Boolean = true,
    val customProps: Map<String, String> = emptyMap(),
    val enabled: Boolean = true
) : Parcelable {

    constructor(parcel: Parcel) : this(
        packageName = parcel.readString() ?? "",
        hideRoot = parcel.readInt() != 0,
        hideSuBinary = parcel.readInt() != 0,
        hideMagiskFiles = parcel.readInt() != 0,
        hideProps = parcel.readInt() != 0,
        customProps = parcel.readHashMap(String::class.java.classLoader) ?: emptyMap(),
        enabled = parcel.readInt() != 0
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(packageName)
        parcel.writeInt(if (hideRoot) 1 else 0)
        parcel.writeInt(if (hideSuBinary) 1 else 0)
        parcel.writeInt(if (hideMagiskFiles) 1 else 0)
        parcel.writeInt(if (hideProps) 1 else 0)
        parcel.writeMap(customProps)
        parcel.writeInt(if (enabled) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<HideRule> = object : Parcelable.Creator<HideRule> {
            override fun createFromParcel(parcel: Parcel): HideRule = HideRule(parcel)
            override fun newArray(size: Int): Array<HideRule?> = arrayOfNulls(size)
        }
    }
}