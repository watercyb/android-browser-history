package com.blueberryjoy.history.history.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.blueberryjoy.history.App
import com.blueberryjoy.history.R
import java.io.File
import java.io.FileOutputStream
import java.net.MalformedURLException
import java.net.URL

object Favicon {
    private val icons = HashMap<String?, Bitmap?>()
    private val illegalChar = arrayOf<String?>(
        "/",
        "\n",
        "\r",
        "\t",
        "\u0000",
        "\u000c",
        "`",
        "?",
        "*",
        "\\",
        "<",
        ">",
        "|",
        "\"",
        ":",
        "<",
        ">",
        "+",
        "[",
        "]",
        ".",
        " "
    )
    private var dir = ""

    fun initialize() {
        icons.clear()
        icons["null"] =
            BitmapFactory.decodeResource(App.getAppContext().resources, R.mipmap.ic_favicon)
        dir = App.getAppContext().filesDir.absolutePath
    }

    fun setIcon(k: String?, bitmap: Bitmap?) {
        icons[k] = bitmap
    }

    fun getIconFromK(k: String): Bitmap? {
        if (icons.containsKey(k)) {
            return icons[k]
        } else {
            val bitmap = getIconFromFile(k)
            setIcon(k, bitmap)
            return bitmap
        }
    }

    fun getIconFromUrl(url: String): Bitmap? {
        val k = urlToFilename(url)
        return getIconFromK(k)
    }

    fun getIconFromFile(filename: String?): Bitmap? {
        return if (checkFileExist("$dir/favicons/$filename.png")) {
            BitmapFactory.decodeFile("$dir/favicons/$filename.png")
        } else {
            null
        }
    }

    fun urlToFilename(url: String): String {
        try {
            if (url.startsWith("file") && url.length > 5) {
                val detail = url.substring(0, url.length - 5).split("@".toRegex())
                    .dropLastWhile { it.isEmpty() }.toTypedArray()
                if (detail.size == 3) return detail[2]
            }
            val strArr =
                URL(url).host.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (strArr.size <= 1) return "null"
            return fileNameValidation(strArr[strArr.size - 1] + strArr[strArr.size - 2])
        } catch (_: MalformedURLException) {
            return "null"
        }
    }

    private fun checkFileExist(fullFilename: String): Boolean {
        val file = File(fullFilename)
        return file.exists()
    }

    private fun fileNameValidation(filename: String): String {
        var filename = filename
        for (str in illegalChar) {
            filename = filename.replace(str!!, "")
        }
        return filename
    }

    fun saveFavicon(icon: Bitmap, url: String) {
        try {
            if (!checkDirExist("$dir/favicons/")) throw Exception()
            val filename = urlToFilename(url)
            setIcon(filename, icon)
            val file = File("$dir/favicons/$filename.png")
            val fileOutputStream = FileOutputStream(file, false)
            icon.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (_: Exception) {
            Message.toast("Cannot save icon")
        }
    }

    fun checkDirExist(fullDir: String): Boolean {
        val dir = File(fullDir)
        if (!dir.exists() || !dir.isDirectory) {
            return dir.mkdirs()
        }
        return true
    }
}
