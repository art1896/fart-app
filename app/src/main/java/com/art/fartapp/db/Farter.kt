package com.art.fartapp.db

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.art.fartapp.util.getRandomColor
import kotlinx.android.parcel.Parcelize
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "farter_table")
@Parcelize
data class Farter(
    val token: String,
    val name: String,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val created: Long = System.currentTimeMillis(),
    val color: Int = getRandomColor()
) : Parcelable {
    val createdDateFormatted: String
        get() {
            val sdf = SimpleDateFormat("MMM dd/yyyy HH:mm", Locale.US)
            val resultDate = Date(created)
            return sdf.format(resultDate)
        }
}