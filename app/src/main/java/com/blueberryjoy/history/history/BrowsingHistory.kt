package com.blueberryjoy.history.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blueberryjoy.history.history.historypage.HistoryItem
import com.blueberryjoy.history.history.sqlite.SQLiteHistory
import com.blueberryjoy.history.history.utils.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.locks.ReentrantLock

object BrowsingHistory : ViewModel() {
    private val SQLite: SQLiteHistory = SQLiteHistory()
    private var lock = ReentrantLock()
    private var historyURLs = HistoryURLs()
    private var historyTokens = HistoryTokens()
    private var historyTrie = HistoryTrie()
    private var debouncedInserter = DebouncedInserter()

    init {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                lock.lock()
                val start = System.currentTimeMillis()
                historyURLs = HistoryURLs()
                historyTokens = HistoryTokens()
                historyTrie = HistoryTrie()
                val histories = SQLite.getHistories()
                val historyURLMap = historyURLs.addAll(histories)
                val historyTokenMap = historyTokens.addAll(historyURLMap)
                historyTrie.addAll(historyTokenMap)
                val end = System.currentTimeMillis()
                Message.log("Initialize: ${end - start}, URLs: ${historyURLMap.size}")
            } finally {
                lock.unlock()
            }
        }
    }

    fun initialize() {}

    fun createHistory(url: String, title: String): HistoryURL {
        val history = SQLite.create(url, title, System.currentTimeMillis())
        return HistoryURL(history.id, history.url, history.title, history.timestamp)
    }

    fun recoverHistory(historyURL: HistoryURL) {
        SQLite.recover(historyURL)
    }

    fun updateHistory(id: Int, title: String): Long {
        val timestamp = System.currentTimeMillis()
        SQLite.update(id, title, timestamp)
        return timestamp
    }

    fun deleteHistory(historyURL: HistoryURL) {
        SQLite.delete(historyURL.id)
        historyURL.removed = true
    }

    fun insertToTokens(historyURL: HistoryURL) {
        historyTokens.insert(historyURL)
    }

    fun insertToTrie(token: String, list: List<HistoryURL>) {
        historyTrie.insert(token, list)
    }

    fun search(searchString: String): ArrayList<HistoryURL> {
        if (lock.isLocked) return ArrayList()
        val start = System.currentTimeMillis()
        var list = ArrayList<HistoryURL>()
        if (searchString.isEmpty()) {
            list = historyURLs.getList()
        } else {
            val set = HashSet<HistoryURL>()
            for (list in historyTrie.get(
                searchString
            )) {
                for (historyURL in list) {
                    set.add(historyURL)
                }
            }
            for (historyURL in set) {
                if (!historyURL.removed) list.add(historyURL)
            }
            list.sortWith(Comparator { o1, o2 -> o2.timestamp.compareTo(o1.timestamp) })
        }
        val seen = HashMap<String, Int>()
        val res = ArrayList<HistoryURL>()
        for (historyURL in list) {
            val key = historyURL.domain.substringBefore("/")
            val count = seen.getOrDefault(key, 0)
            if (count < 3) {
                res.add(historyURL)
                seen[key] = count + 1
            }
        }
        val end = System.currentTimeMillis()
        Message.log("Search: ${end - start}")
        return res
    }

    fun insert(url: String, title: String?) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                lock.lock()
                val start = System.currentTimeMillis()
                if (title == null) {
                    historyURLs.insertURL(url, "<NO TITLE>")
                } else {
                    historyURLs.insertURL(url, title)
                }
                val end = System.currentTimeMillis()
                Message.log("Update: ${end - start}")
            } finally {
                lock.unlock()
            }
        }
    }

    fun debouncedInsert(url: String, title: String) {
        debouncedInserter.insert(url, title)
    }

    fun getList(): ArrayList<HistoryItem> {
        try {
            lock.lock()
            val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
            val items = ArrayList<HistoryItem>()
            var lastDate: LocalDate? = null
            for (history in historyURLs.getList()) {
                if (history.removed) continue
                val date = Instant.ofEpochMilli(history.timestamp).atZone(ZoneId.systemDefault())
                    .toLocalDate()
                if (date != lastDate) {
                    items.add(
                        HistoryItem.Header(
                            when (date) {
                                LocalDate.now() -> "Today - ${date.format(formatter)}"
                                LocalDate.now()
                                    .minusDays(1) -> "Yesterday - ${date.format(formatter)}"

                                else -> date.format(formatter)
                            }
                        )
                    )
                    lastDate = date
                }
                items.add(HistoryItem.Entry(history))
            }
            return items
        } finally {
            lock.unlock()
        }
    }

    fun clear() {
        try {
            lock.lock()
            historyURLs = HistoryURLs()
            historyTokens = HistoryTokens()
            historyTrie = HistoryTrie()
            SQLite.deleteAll()
        } finally {
            lock.unlock()
        }
    }
}