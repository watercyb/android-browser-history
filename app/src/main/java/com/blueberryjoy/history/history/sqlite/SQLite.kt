package com.blueberryjoy.history.history.sqlite

import androidx.room.Room
import androidx.room.RoomDatabase
import com.blueberryjoy.history.App

object SQLite {

    val db: AppDatabase by lazy {
        Room.databaseBuilder(
            App.appContext,
            AppDatabase::class.java,
            "app_db"
        )
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
            .build()
    }
}

