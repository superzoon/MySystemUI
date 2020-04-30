package cn.nubia.systemui

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import cn.nubia.systemui.common.Controller
import cn.nubia.systemui.common.UpdateMonitor

class NubiaSystemUIApplication: Application() {
    init {
        mContext = this
    }

    var mHardKeyboardHidden = Configuration.KEYBOARDHIDDEN_UNDEFINED
    companion object {
        val TAG = "NSUI"
        private var mContext:NubiaSystemUIApplication?=null
        fun getContext(): Context = mContext!!
    }

    override fun onCreate() {
        super.onCreate()
        Controller.init(this)
        onConfigurationChanged(resources.configuration)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Controller.forEach { it.callConfigurationChanged(newConfig) }
    }

}