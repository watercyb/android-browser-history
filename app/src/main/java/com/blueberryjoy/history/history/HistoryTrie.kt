package com.blueberryjoy.history.history

import kotlin.text.iterator

class HistoryTrie {
    private val root = HistoryTrieNode()

    fun insert(token: String, list: List<HistoryURL>) {
        var node = root
        for (chr in token) {
            node.add(list)
            node = node.nextOrCreate(chr)
        }
        node.add(list)
    }

    fun get(token: String): List<List<HistoryURL>> {
        var node = root
        for (chr in token) {
            val next = node.next(chr) ?: break
            node = next
        }
        return node.getList()
    }

    fun addAll(historyTokenMap: HashMap<String, ArrayList<HistoryURL>>) {
        for ((token, list) in historyTokenMap.entries) {
            insert(token, list)
        }
    }
}