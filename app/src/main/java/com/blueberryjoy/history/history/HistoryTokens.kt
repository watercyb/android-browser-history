package com.blueberryjoy.history.history

class HistoryTokens {
    private val map = HashMap<String, ArrayList<HistoryURL>>()

    fun insert(historyURL: HistoryURL) {
        val tokens = getTokens(historyURL.domain)
        for (token in tokens) {
            if (!map.containsKey(token)) {
                val list = ArrayList<HistoryURL>()
                list.add(historyURL)
                map[token] = list
                BrowsingHistory.insertToTrie(token, list)
            } else {
                map[token]!!.add(historyURL)
            }
        }
    }

    fun getTokens(domain: String): ArrayList<String> {
        val res = ArrayList<String>()
        val host = domain.substringBefore('/')
        val path = domain.substringAfter('/', "")
        res.addAll(host.split(".").filter { it.isNotEmpty() })
        res.addAll(path.split("/").filter { it.isNotEmpty() })
        return res
    }

    fun addAll(historyURLMap: HashMap<String, HistoryURL>): HashMap<String, ArrayList<HistoryURL>> {
        for (historyURL in historyURLMap.values) {
            for (token in getTokens(historyURL.domain)) {
                if (!map.containsKey(token)) {
                    val list = ArrayList<HistoryURL>()
                    list.add(historyURL)
                    map[token] = list
                } else {
                    map[token]!!.add(historyURL)
                }
            }
        }
        return map
    }
}