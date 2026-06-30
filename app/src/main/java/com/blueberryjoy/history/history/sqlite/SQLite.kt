package com.blueberryjoy.history.history.sqlite

import androidx.room.Room
import com.blueberryjoy.history.App
import com.blueberryjoy.history.history.utils.Message

object SQLite {

    private var db: AppDatabase? = null

    fun initialize() {
        try {
            db = Room.databaseBuilder(
                App.getAppContext(),
                AppDatabase::class.java,
                "app_db"
            ).build()
        } catch (e: Exception) {
            Message.log("DB ERROR: ${e.message}")
            e.printStackTrace()
        }
    }

    fun getDb(): AppDatabase {
        if (db == null) initialize()
        return db!!
    }
}

