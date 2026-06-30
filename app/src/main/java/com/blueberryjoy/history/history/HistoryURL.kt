package com.blueberryjoy.history.history

import android.graphics.Bitmap
import com.blueberryjoy.history.history.utils.Favicon
import java.net.URI
import java.time.Instant
import java.time.ZoneId

class HistoryURL(var id: Int, val url: String, var title: String, var timestamp: Long) {
    var icon: Bitmap? = Favicon.getIconFromUrl(url)
    var domain = getDomain(url)
    var removed = false

    fun update(title: String) {
        this.title = title
        timestamp = BrowsingHistory.updateHistory(id, title)
    }

    fun getDomain(url: String): String {
        val uri = URI(url)
        val host = uri.host ?: return url
        val path = uri.path.substringBeforeLast(".")
        return host + path
    }

    override fun toString(): String {
        return "id=$id, url='$url', timestamp=${
            Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime()
        }"
    }
}