package cn.nubia.systemui.ext

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import cn.nubia.systemui.NubiaSystemUIApplication
import cn.nubia.systemui.NubiaSystemUIService
import cn.nubia.systemui.fingerprint.FingerprintController
import cn.nubia.systemui.fingerprint.FingerprintWindowController
import java.io.FileDescriptor
import java.io.PrintWriter

abstract class Controller(val mContext:Context) {
    fun start(service:NubiaSystemUIService){}
    fun onTrimMemory(level: Int) {}
    fun stop(service:NubiaSystemUIService){}
    fun dump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?) {}

    fun <T> getController(name:Class<T>) :T = Controller.getController(name)

    companion object {
        private val mMap = mutableMapOf<Class<*>, Controller>()

        fun forEach(action: (Controller) -> Unit) = mMap.values.forEach(action)

        fun init(context:NubiaSystemUIApplication){
            mMap.put(FingerprintController::class.java, FingerprintController(context))
            mMap.put(FingerprintWindowController::class.java, FingerprintWindowController(context))
        }

        fun <T> getController(name:Class<T>):T =  mMap.get(name)!! as T
    }
}