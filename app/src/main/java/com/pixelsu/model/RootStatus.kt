package com.pixelsu.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.serialization.Serializable

@Serializable
data class RootStatus(
    val isRooted: Boolean = false,
    val rootType: RootType = RootType.UNKNOWN,
    val rootVersion: String = "",
    val hasMagisk: Boolean = false,
    val magiskVersion: String = "",
    val magiskVersionCode: Int = 0,
    val hasKernelSU: Boolean = false,
    val kernelSUVersion: String = "",
    val hasSuperSU: Boolean = false,
    val superSUVersion: String = "",
    val hasPHHSuperuser: Boolean = false,
    val phhVersion: String = "",
    val isTempRootActive: Boolean = false,
    val tempRootEndTime: Long = 0,
    val isHideActive: Boolean = false,
    val hideMode: HideMode = HideMode.NONE,
    val selinuxStatus: SELinuxStatus = SELinuxStatus.UNKNOWN,
    val kernelVersion: String = "",
    val deviceModel: String = "",
    val androidVersion: String = "",
    val apiLevel: Int = 0,
    val architecture: String = "",
    val uptime: Long = 0,
    val lastChecked: Long = System.currentTimeMillis()
) : Parcelable {

    constructor(parcel: Parcel) : this(
        isRooted = parcel.readInt() != 0,
        rootType = RootType.valueOf(parcel.readString()!!),
        rootVersion = parcel.readString()!!,
        hasMagisk = parcel.readInt() != 0,
        magiskVersion = parcel.readString()!!,
        magiskVersionCode = parcel.readInt(),
        hasKernelSU = parcel.readInt() != 0,
        kernelSUVersion = parcel.readString()!!,
        hasSuperSU = parcel.readInt() != 0,
        superSUVersion = parcel.readString()!!,
        hasPHHSuperuser = parcel.readInt() != 0,
        phhVersion = parcel.readString()!!,
        isTempRootActive = parcel.readInt() != 0,
        tempRootEndTime = parcel.readLong(),
        isHideActive = parcel.readInt() != 0,
        hideMode = HideMode.valueOf(parcel.readString()!!),
        selinuxStatus = SELinuxStatus.valueOf(parcel.readString()!!),
        kernelVersion = parcel.readString()!!,
        deviceModel = parcel.readString()!!,
        androidVersion = parcel.readString()!!,
        apiLevel = parcel.readInt(),
        architecture = parcel.readString()!!,
        uptime = parcel.readLong(),
        lastChecked = parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(if (isRooted) 1 else 0)
        parcel.writeString(rootType.name)
        parcel.writeString(rootVersion)
        parcel.writeInt(if (hasMagisk) 1 else 0)
        parcel.writeString(magiskVersion)
        parcel.writeInt(magiskVersionCode)
        parcel.writeInt(if (hasKernelSU) 1 else 0)
        parcel.writeString(kernelSUVersion)
        parcel.writeInt(if (hasSuperSU) 1 else 0)
        parcel.writeString(superSUVersion)
        parcel.writeInt(if (hasPHHSuperuser) 1 else 0)
        parcel.writeString(phhVersion)
        parcel.writeInt(if (isTempRootActive) 1 else 0)
        parcel.writeLong(tempRootEndTime)
        parcel.writeInt(if (isHideActive) 1 else 0)
        parcel.writeString(hideMode.name)
        parcel.writeString(selinuxStatus.name)
        parcel.writeString(kernelVersion)
        parcel.writeString(deviceModel)
        parcel.writeString(androidVersion)
        parcel.writeInt(apiLevel)
        parcel.writeString(architecture)
        parcel.writeLong(uptime)
        parcel.writeLong(lastChecked)
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<RootStatus> = object : Parcelable.Creator<RootStatus> {
            override fun createFromParcel(parcel: Parcel): RootStatus = RootStatus(parcel)
            override fun newArray(size: Int): Array<RootStatus?> = arrayOfNulls(size)
        }
    }

    val tempRootRemaining: Long
        get() = if (isTempRootActive && tempRootEndTime > 0) {
            maxOf(0, tempRootEndTime - System.currentTimeMillis())
        } else 0

    val isTempRootExpired: Boolean
        get() = isTempRootActive && tempRootRemaining <= 0

    val statusSummary: String
        get() = when {
            isTempRootActive && !isTempRootExpired -> "Temp Root Active (${formatDuration(tempRootRemaining)})"
            isTempRootActive && isTempRootExpired -> "Temp Root Expired"
            isRooted && isHideActive -> "Rooted + Hidden ($hideMode)"
            isRooted -> "Rooted ($rootType)"
            else -> "Not Rooted"
        }

    private fun formatDuration(millis: Long): String {
        val minutes = millis / 60000
        val seconds = (millis % 60000) / 1000
        return if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
    }
}

enum class RootType {
    UNKNOWN,
    MAGISK,
    KERNELSU,
    SUPERSU,
    PHH_SUPERUSER,
    CUSTOM
}

enum class HideMode {
    NONE,
    MAGISK_HIDE,
    ZYGISK,
    SHAMIKO,
    KERNEL_HIDE,
    DENY_LIST,
    ALLOW_LIST
}

enum class SELinuxStatus {
    UNKNOWN,
    ENFORCING,
    PERMISSIVE,
    DISABLED
}
