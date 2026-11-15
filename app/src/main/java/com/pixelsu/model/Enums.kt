package com.pixelsu.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.serialization.Serializable

@Serializable
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

@Serializable
enum class ShellType {
    SU,
    SH,
    BASH,
    ZSH,
    TOYBOX,
    TOOLBOX
}

@Serializable
enum class SELinuxMode {
    ENFORCING,
    PERMISSIVE,
    DISABLED
}

@Serializable
enum class HideListMode {
    DENY_LIST,
    ALLOW_LIST
}

@Serializable
class HideListConfig(
    val mode: HideListMode = HideListMode.DENY_LIST,
    val targetPackages: List<String> = emptyList(),
    val hideFromMagiskHide: Boolean = true,
    val hideFromZygisk: Boolean = true,
    val hideFromShamiko: Boolean = true,
    val hideFromKernel: Boolean = true,
    val includeSystemApps: Boolean = false,
    val customRules: List<HideRule> = emptyList(),
    val lastModified: Long = System.currentTimeMillis()
) : Parcelable {

    constructor(parcel: Parcel) : this(
        mode = HideListMode.valueOf(parcel.readString()!!),
        targetPackages = parcel.createStringArrayList() ?: emptyList(),
        hideFromMagiskHide = parcel.readInt() != 0,
        hideFromZygisk = parcel.readInt() != 0,
        hideFromShamiko = parcel.readInt() != 0,
        hideFromKernel = parcel.readInt() != 0,
        includeSystemApps = parcel.readInt() != 0,
        customRules = parcel.readArrayList(HideRule::class.java.classLoader) as List<HideRule> ?: emptyList(),
        lastModified = parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(mode.name)
        parcel.writeStringList(targetPackages)
        parcel.writeInt(if (hideFromMagiskHide) 1 else 0)
        parcel.writeInt(if (hideFromZygisk) 1 else 0)
        parcel.writeInt(if (hideFromShamiko) 1 else 0)
        parcel.writeInt(if (hideFromKernel) 1 else 0)
        parcel.writeInt(if (includeSystemApps) 1 else 0)
        parcel.writeList(customRules)
        parcel.writeLong(lastModified)
    }

    override fun describeContents(): Int = 0

    fun copy(
        mode: HideListMode = this.mode,
        targetPackages: List<String> = this.targetPackages,
        hideFromMagiskHide: Boolean = this.hideFromMagiskHide,
        hideFromZygisk: Boolean = this.hideFromZygisk,
        hideFromShamiko: Boolean = this.hideFromShamiko,
        hideFromKernel: Boolean = this.hideFromKernel,
        includeSystemApps: Boolean = this.includeSystemApps,
        customRules: List<HideRule> = this.customRules,
        lastModified: Long = this.lastModified
    ): HideListConfig {
        return HideListConfig(
            mode = mode,
            targetPackages = targetPackages,
            hideFromMagiskHide = hideFromMagiskHide,
            hideFromZygisk = hideFromZygisk,
            hideFromShamiko = hideFromShamiko,
            hideFromKernel = hideFromKernel,
            includeSystemApps = includeSystemApps,
            customRules = customRules,
            lastModified = lastModified
        )
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<HideListConfig> = object : Parcelable.Creator<HideListConfig> {
            override fun createFromParcel(parcel: Parcel): HideListConfig = HideListConfig(parcel)
            override fun newArray(size: Int): Array<HideListConfig?> = arrayOfNulls(size)
        }

        fun getDefaults(): HideListConfig = HideListConfig()
    }
}
