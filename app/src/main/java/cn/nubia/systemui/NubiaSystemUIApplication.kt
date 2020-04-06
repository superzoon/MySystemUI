package cn.nubia.systemui

import android.app.Application
import cn.nubia.systemui.ext.Controller

class NubiaSystemUIApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        Controller.init(this)
    }

}