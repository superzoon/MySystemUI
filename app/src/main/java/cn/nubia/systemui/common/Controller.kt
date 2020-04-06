package cn.nubia.systemui.ext

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import cn.nubia.systemui.NubiaSystemUIApplication
import cn.nubia.systemui.fingerprint.FingerprintController
import cn.nubia.systemui.fingerprint.FingerprintWindowController

abstract class Controller(val mContext:Context) {
    companion object {
        private val mMap = mutableMapOf<Class<*>, Any>()

        fun init(context:NubiaSystemUIApplication){
            mMap.put(FingerprintController::class.java, FingerprintController(context))
            mMap.put(FingerprintWindowController::class.java, FingerprintWindowController(context))
        }

        fun <T> getController(name:Class<T>):T =  mMap.get(name)!! as T
    }
}