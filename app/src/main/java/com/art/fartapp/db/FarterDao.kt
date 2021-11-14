package com.art.fartapp.db

import androidx.room.*
import com.art.fartapp.data.SortOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface FarterDao {

    fun getFarters(query: String, sortOrder: SortOrder): Flow<List<Farter>> =
        when(sortOrder) {
            SortOrder.BY_DATE -> getFartersSortedByDate(query)
            SortOrder.BY_NAME -> getFartersSortedByName(query)
        }

    @Insert
    suspend fun insertFarter(farter: Farter)

    @Delete
    suspend fun deleteFarter(farter: Farter)

    @Update
    suspend fun update(farter: Farter)

    @Query("SELECT * FROM farter_table WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name")
    fun getFartersSortedByName(searchQuery: String): Flow<List<Farter>>

    @Query("SELECT * FROM farter_table WHERE name LIKE '%' || :searchQuery || '%' ORDER BY created DESC")
    fun getFartersSortedByDate(searchQuery: String): Flow<List<Farter>>

    @Query("DELETE FROM farter_table")
    suspend fun deleteAllFarters()

}
