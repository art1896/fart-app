package com.art.fartapp.db

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.text.DateFormat

@Entity(tableName = "farter_table")
@Parcelize
data class Farter(
    val token: String,
    val name: String,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val created: Long = System.currentTimeMillis()
): Parcelable {
    val createdDateFormatted: String
        get() = DateFormat.getDateTimeInstance().format(created)
}