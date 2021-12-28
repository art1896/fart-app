package com.art.fartapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.art.fartapp.di.ApplicationScope
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider


@Database(entities = [Farter::class], version = 1)
abstract class FarterDatabase : RoomDatabase() {

    abstract fun farterDao(): FarterDao

    class Callback @Inject constructor(
        private val database: Provider<FarterDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val dao = database.get().farterDao()

            FirebaseMessaging.getInstance().token.addOnSuccessListener {
                applicationScope.launch {
                    dao.insertFarter(Farter(it, "MySelf"))
                }
            }
        }
    }
}