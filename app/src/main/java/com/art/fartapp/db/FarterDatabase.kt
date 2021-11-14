package com.art.fartapp.db

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [Farter::class], version = 1)
abstract class FarterDatabase: RoomDatabase() {

    abstract fun farterDao(): FarterDao
}