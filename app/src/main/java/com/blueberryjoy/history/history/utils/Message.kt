package com.blueberryjoy.history.history.utils

import android.util.Log
import android.widget.Toast
import com.blueberryjoy.history.App

object Message {
    fun toast(str: String?) {
        Toast.makeText(App.appContext, str, Toast.LENGTH_SHORT).show()
    }

    fun log(str: String) {
        Log.w("TestLog", str)
    }
}
