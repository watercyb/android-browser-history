package com.blueberryjoy.history.history

class HistoryTrieNode {
    private val list = ArrayList<List<HistoryURL>>()
    private val children = HashMap<Char, HistoryTrieNode>()

    fun next(chr: Char): HistoryTrieNode? {
        return children[chr]
    }

    fun nextOrCreate(chr: Char): HistoryTrieNode {
        if (children[chr] == null)
            children[chr] = HistoryTrieNode()
        return children[chr]!!
    }

    fun add(list: List<HistoryURL>) {
        this.list.add(list)
    }

    fun getList(): List<List<HistoryURL>> {
        return list
    }
}