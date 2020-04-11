package cn.nubia.systemui

import android.app.Application
import android.content.Context
import android.util.Log
import cn.nubia.systemui.common.Controller

class NubiaSystemUIApplication: Application() {
    init {
        mContext = this
    }

    companion object {
        val TAG = "NubSysUI"
        private var mContext:NubiaSystemUIApplication?=null
        fun getContext(): Context = mContext!!
    }

    override fun onCreate() {
        super.onCreate()
        Controller.init(this)
    }

}