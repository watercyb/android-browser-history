package com.blueberryjoy.history.history.sqlite

import com.blueberryjoy.history.history.utils.Message
import com.blueberryjoy.history.history.HistoryUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId

const val MAX_HISTORY_COUNT = 100000

class HistoryRepository(private val dao: HistoryDao) {

    suspend fun loadHistories(): List<History> = withContext(Dispatchers.IO) {
        val retentionCutoff: Instant =
            Instant.now().atZone(ZoneId.systemDefault()).minusYears(1).toInstant()
        val histories = mutableListOf<History>()
        try {
            val allHistories = dao.allHistories()
            val visitedUrls = hashSetOf<String>()
            var remainingSlots = MAX_HISTORY_COUNT
            for (history in allHistories) {
                // Delete duplicate URLs, entries older than one year, and any entries beyond the maximum history limit.
                if (!visitedUrls.add(history.url) || Instant.ofEpochMilli(history.timestamp)
                        .isBefore(retentionCutoff) || remainingSlots <= 0
                ) {
                    deleteHistory(history.id)
                } else {
                    histories.add(history)
                    remainingSlots--
                }
            }
        } catch (e: Exception) {
            Message.log("SQLite Error: $e")
        }
        histories
    }

    suspend fun create(url: String, title: String, timestamp: Long): History =
        withContext(Dispatchers.IO) {
            val history = History(url, title, timestamp)
            try {
                val id: Long = dao.insert(history)
                history.id = id.toInt()
            } catch (e: Exception) {
                Message.log("SQLite Error: $e")
            }
            history
        }

    suspend fun restore(historyURL: HistoryUrl) {
        try {
            val id: Long =
                dao.insert(History(historyURL.url, historyURL.title, historyURL.timestamp))
            historyURL.id = id.toInt()
        } catch (e: Exception) {
            Message.log("SQLite Error: $e")
        }
    }

    suspend fun updateHistory(id: Int, title: String, timestamp: Long) {
        dao.updateHistory(id, title, timestamp)
    }

    suspend fun deleteHistory(id: Int) {
        dao.deleteHistory(id)
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }
}