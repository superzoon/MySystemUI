package cn.nubia.systemui

import android.app.Application
import android.util.Log
import cn.nubia.systemui.ext.Controller

class NubiaSystemUIApplication: Application() {
    companion object {
        val TAG = "NubSysUI"
    }
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "NubiaSystemUIApplication onCreate start")
        Controller.init(this)
        Log.i(TAG, "NubiaSystemUIApplication onCreate end")
    }

}