package com.art.fartapp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SoundDao {

    @Insert
    suspend fun insertSound(sound: Sound)

    @Query("SELECT * FROM sound_table")
    fun getAllSounds(): Flow<List<Sound>>

}