package com.art.fartapp.db

import android.app.Application
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.art.fartapp.R
import com.art.fartapp.di.ApplicationScope
import com.art.fartapp.util.getResourceName
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider


@Database(entities = [Farter::class, AcceptedFarts::class, Sound::class], version = 4)
abstract class FarterDatabase : RoomDatabase() {

    abstract fun farterDao(): FarterDao

    abstract fun soundDao(): SoundDao

    class Callback @Inject constructor(
        private val database: Provider<FarterDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope,
        private val app: Application
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val dao = database.get().farterDao()
            val soundDao = database.get().soundDao()

            FirebaseMessaging.getInstance().token.addOnSuccessListener {
                applicationScope.launch {
                    dao.insertFarter(Farter(it, "MySelf"))
                }
            }

            applicationScope.launch {
                soundDao.insertSound(
                    Sound(
                        name = "Fart",
                        duration = "00:25",
                        iconRes = app.applicationContext.getResourceName(R.drawable.fart),
                        rawRes = app.applicationContext.getResourceName(R.raw.fart)
                    )
                )

                soundDao.insertSound(
                    Sound(
                        name = "Wont Start Fart",
                        duration = "00:02",
                        iconRes = app.applicationContext.getResourceName(R.drawable.fart),
                        rawRes = app.applicationContext.getResourceName(R.raw.wont_start_fart)
                    )
                )
                soundDao.insertSound(
                    Sound(
                        name = "Silly Farts Joe",
                        duration = "00:04",
                        iconRes = app.applicationContext.getResourceName(R.drawable.fart),
                        rawRes = app.applicationContext.getResourceName(R.raw.silly_farts_joe)
                    )
                )
                soundDao.insertSound(
                    Sound(
                        name = "Wet Fart Squish",
                        duration = "00:02",
                        iconRes = app.applicationContext.getResourceName(R.drawable.fart),
                        rawRes = app.applicationContext.getResourceName(R.raw.wet_fart_squish)
                    )
                )
                soundDao.insertSound(
                    Sound(
                        name = "Fart Common Everyday",
                        duration = "00:02",
                        iconRes = app.applicationContext.getResourceName(R.drawable.fart),
                        rawRes = app.applicationContext.getResourceName(R.raw.fart_common_everyday)
                    )
                )
                soundDao.insertSound(
                    Sound(
                        name = "Fart Squeeze Yer Knees",
                        duration = "00:02",
                        iconRes = app.applicationContext.getResourceName(R.drawable.fart),
                        rawRes = app.applicationContext.getResourceName(R.raw.fart_squeeze_yerknees)
                    )
                )
                soundDao.insertSound(
                    Sound(
                        name = "Burp",
                        duration = "00:02",
                        iconRes = app.applicationContext.getResourceName(R.drawable.burp),
                        rawRes = app.applicationContext.getResourceName(R.raw.burp)
                    )
                )
                soundDao.insertSound(
                    Sound(
                        name = "Belch",
                        duration = "00:02",
                        iconRes = app.applicationContext.getResourceName(R.drawable.burp),
                        rawRes = app.applicationContext.getResourceName(R.raw.belch)
                    )
                )
                soundDao.insertSound(
                    Sound(
                        name = "Burp 2x",
                        duration = "00:02",
                        iconRes = app.applicationContext.getResourceName(R.drawable.burp),
                        rawRes = app.applicationContext.getResourceName(R.raw.burp_2x)
                    )
                )
                soundDao.insertSound(
                    Sound(
                        name = "Super Burp",
                        duration = "00:06",
                        iconRes = app.applicationContext.getResourceName(R.drawable.burp),
                        rawRes = app.applicationContext.getResourceName(R.raw.super_burp)
                    )
                )
            }
        }
    }
}