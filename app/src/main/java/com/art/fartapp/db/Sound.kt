package com.art.fartapp.db

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "sound_table")
@Parcelize
data class Sound(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val duration: String,
    val iconRes: String,
    val rawRes: String
) : Parcelable