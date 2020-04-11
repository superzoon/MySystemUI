package cn.nubia.systemui.ext

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import cn.nubia.systemui.NubiaSystemUIApplication
import cn.nubia.systemui.NubiaSystemUIService
import cn.nubia.systemui.fingerprint.FingerprintController
import cn.nubia.systemui.fingerprint.FingerprintWindowController
import java.io.FileDescriptor
import java.io.PrintWriter

abstract class Controller(val mContext:Context) {
    fun onStart(service:NubiaSystemUIService) = Log.i(TAG, "${service}.${this.javaClass.simpleName} on start")
    fun onTrimMemory(level: Int)  = Log.i(TAG, "${this.javaClass.simpleName} onTrimMemory ${level}")
    fun onStop(service:NubiaSystemUIService) = Log.i(TAG, "${service}.${this.javaClass.simpleName} on stop")
    fun dump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?)  = Log.i(TAG, "dump ${fd}.${writer}.${args}")

    fun <T> getController(name:Class<T>) :T = Controller.getController(name)

    companion object {
        private val TAG = "${NubiaSystemUIApplication.TAG}.Controller"
        private val mMap = mutableMapOf<Class<*>, Controller>()

        fun forEach(action: (Controller) -> Unit) =mMap.values.forEach{ action.invoke(it) }

        fun init(context:NubiaSystemUIApplication){
            mMap.put(FingerprintController::class.java, FingerprintController(context))
            mMap.put(FingerprintWindowController::class.java, FingerprintWindowController(context))
            Log.i(TAG,"${mMap.values.size}")
        }

        fun <T> getController(name:Class<T>):T =  mMap.get(name)!! as T
    }
}