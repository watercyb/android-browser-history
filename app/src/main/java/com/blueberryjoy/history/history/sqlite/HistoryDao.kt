package com.blueberryjoy.history.history.sqlite

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HistoryDao {
    @Insert
    fun insert(history: History): Long

    @Query("SELECT * FROM histories")
    fun allHistories(): MutableList<History>

    @Query("DELETE FROM histories WHERE id = :historiesId")
    fun deleteHistory(historiesId: Int)

    @Query("UPDATE histories SET timestamp = :timestamp, title = :title WHERE id = :historiesId")
    fun updateTimestamp(historiesId: Int, title: String, timestamp: Long)

    @Query("DELETE FROM histories")
    fun deleteAll()
}