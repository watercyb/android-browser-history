package com.blueberryjoy.history.history

import com.blueberryjoy.history.history.sqlite.History

class HistoryURLs {
    private val map = HashMap<String, HistoryURL>()
    private val list = ArrayList<HistoryURL>()

    fun insertURL(url: String, title: String) {
        if (map.containsKey(url)) {
            val historyURL = map[url]!!
            if (historyURL.removed) {
                val newHistoryURL = BrowsingHistory.createHistory(url, title)
                historyURL.id = newHistoryURL.id
                historyURL.title = newHistoryURL.title
                historyURL.timestamp = newHistoryURL.timestamp
                historyURL.removed = false
            } else {
                historyURL.update(title)
            }
            var idx = 0
            while (idx < list.size && list[idx] != historyURL) {
                idx++
            }
            if (idx == list.size) {
                val historyURL = BrowsingHistory.createHistory(url, title)
                map[url] = historyURL
                list.add(0, historyURL)
                BrowsingHistory.insertToTokens(historyURL)
            } else {
                for (i in idx downTo 1) {
                    list[i] = list[i - 1]
                }
                list[0] = historyURL
            }
        } else {
            val historyURL = BrowsingHistory.createHistory(url, title)
            map[url] = historyURL
            list.add(0, historyURL)
            BrowsingHistory.insertToTokens(historyURL)
        }
    }

    fun addAll(histories: List<History>): HashMap<String, HistoryURL> {
        for (history in histories) {
            val historyURL =
                HistoryURL(history.id, history.url, history.title, history.timestamp)
            map[history.url] = historyURL
            list.add(historyURL)
        }
        list.sortWith(Comparator { o1, o2 -> o2.timestamp.compareTo(o1.timestamp) })
        return map
    }

    fun getList(): ArrayList<HistoryURL> {
        return list
    }
}