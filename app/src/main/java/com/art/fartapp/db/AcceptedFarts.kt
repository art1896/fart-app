package com.art.fartapp.db

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "accepted_farts_table")
@Parcelize
data class AcceptedFarts(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val accepted: Long = System.currentTimeMillis()
) : Parcelable {
    val createdDateFormatted: String
        get() {
            val sdf = SimpleDateFormat("MMM dd/yyyy HH:mm", Locale.US)
            val resultDate = Date(accepted)
            return sdf.format(resultDate)
        }
}