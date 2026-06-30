package com.blueberryjoy.history.history.sqlite

import com.blueberryjoy.history.history.utils.Message
import com.blueberryjoy.history.history.HistoryURL
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SQLiteHistory {
    val executor: ExecutorService = Executors.newSingleThreadExecutor()
    val maxHistoryCount = 100000
    val historyRetentionCutoff: Instant =
        Instant.now().atZone(ZoneId.systemDefault()).minusYears(1).toInstant()

    fun getHistories(): List<History> {
        val histories: MutableList<History> = ArrayList()
        val future = executor.submit(Callable {
            SQLite.getDb().historyDao().allHistories()
        })
        try {
            val visitedUrls = HashSet<String>()
            var remainingSlots = maxHistoryCount
            val allHistories = future.get()
            allHistories.sortWith { a, b -> b.timestamp.compareTo(a.timestamp) }
            for (history in allHistories) {
                // Delete duplicate URLs, entries older than one year, and any entries beyond the maximum history limit.
                if (!visitedUrls.add(history.url) || Instant.ofEpochMilli(history.timestamp)
                        .isBefore(historyRetentionCutoff) || remainingSlots <= 0
                ) {
                    delete(history.id)
                } else {
                    histories.add(history)
                    remainingSlots--
                }
            }
        } catch (e: Exception) {
            Message.log("SQLite Error: $e")
        }
        return histories
    }

    fun create(url: String, title: String, timestamp: Long): History {
        val history = History(url, title, timestamp)
        val future = executor.submit<Long> { SQLite.getDb().historyDao().insert(history) }
        try {
            val id: Long = future.get()!!
            history.id = id.toInt()
        } catch (e: Exception) {
            Message.log("SQLite Error: $e")
        }
        return history
    }

    fun recover(historyURL: HistoryURL) {
        val future = executor.submit<Long> {
            SQLite.getDb().historyDao()
                .insert(History(historyURL.url, historyURL.title, historyURL.timestamp))
        }
        try {
            val id: Long = future.get()!!
            historyURL.id = id.toInt()
        } catch (e: Exception) {
            Message.log("SQLite Error: $e")
        }
    }

    fun update(id: Int, title: String, timestamp: Long) {
        executor.submit { SQLite.getDb().historyDao().updateTimestamp(id, title, timestamp) }
    }

    fun delete(id: Int) {
        executor.submit { SQLite.getDb().historyDao().deleteHistory(id) }
    }

    fun deleteAll() {
        executor.submit { SQLite.getDb().historyDao().deleteAll() }
    }
}