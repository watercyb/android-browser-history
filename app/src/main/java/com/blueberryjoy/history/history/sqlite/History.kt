package com.blueberryjoy.history.history.sqlite

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.ZoneId

@Entity(tableName = "histories")
class History(@JvmField val url: String, @JvmField val title: String, @JvmField val timestamp: Long) {
    @JvmField
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    override fun toString(): String {
        return "$id $url $title ${
            Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime()
        }"
    }
}