package com.pixelsu.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "logs")
@Serializable
data class LogEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel = LogLevel.INFO,
    val tag: String = "PixelSU",
    val message: String = "",
    val throwable: String? = null,
    val threadName: String = Thread.currentThread().name,
    val category: LogCategory = LogCategory.GENERAL,
    val relatedExploitId: String? = null,
    val relatedPackage: String? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        timestamp = parcel.readLong(),
        level = LogLevel.valueOf(parcel.readString()!!),
        tag = parcel.readString()!!,
        message = parcel.readString()!!,
        throwable = parcel.readString(),
        threadName = parcel.readString()!!,
        category = LogCategory.valueOf(parcel.readString()!!),
        relatedExploitId = parcel.readString(),
        relatedPackage = parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(timestamp)
        parcel.writeString(level.name)
        parcel.writeString(tag)
        parcel.writeString(message)
        parcel.writeString(throwable)
        parcel.writeString(threadName)
        parcel.writeString(category.name)
        parcel.writeString(relatedExploitId)
        parcel.writeString(relatedPackage)
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<LogEntry> = object : Parcelable.Creator<LogEntry> {
            override fun createFromParcel(parcel: Parcel): LogEntry = LogEntry(parcel)
            override fun newArray(size: Int): Array<LogEntry?> = arrayOfNulls(size)
        }
    }

    val formattedTimestamp: String
        get() = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", java.util.Locale.getDefault())
            .format(java.util.Date(timestamp))

    val shortMessage: String
        get() = if (message.length > 100) "${message.substring(0, 100)}..." else message
}

enum class LogLevel {
    VERBOSE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
    ASSERT
}

enum class LogCategory {
    GENERAL,
    ROOT_CHECK,
    ROOT_ENABLE,
    ROOT_DISABLE,
    TEMP_ROOT,
    ROOT_HIDE,
    EXPLOIT,
    EXPLOIT_EXECUTION,
    SETTINGS,
    BOOT,
    PACKAGE_CHANGE,
    ERROR,
    SECURITY
}
