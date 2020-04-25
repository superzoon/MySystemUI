package cn.nubia.systemui.common

import android.content.Context
import android.content.res.Configuration
import android.os.Handler
import android.util.Log
import cn.nubia.systemui.NubiaSystemUIApplication
import cn.nubia.systemui.NubiaSystemUIService
import cn.nubia.systemui.fingerprint.FingerprintController
import cn.nubia.systemui.fingerprint.FingerprintWindowController
import java.io.FileDescriptor
import java.io.PrintWriter

abstract class Controller(val mContext:Context):Dump {
    abstract fun getHandler():Handler
    abstract fun onStart(service:NubiaSystemUIService)
    abstract fun onStop(service:NubiaSystemUIService)
    protected fun onConfigurationChanged(config: Configuration){}
    protected fun onTrimMemory(level: Int){}

    fun callStart(service:NubiaSystemUIService){
        registerDump()
        getHandler().post{onStart(service)}
    }

    fun callStop(service:NubiaSystemUIService){
        unregisterDump()
        getHandler().post{onStop(service)}
    }

    fun callTrimMemory(level: Int){
        getHandler().post{onTrimMemory(level)}
    }

    fun callConfigurationChanged(config: Configuration){
        getHandler().post{onConfigurationChanged(config)}
    }


    override fun dump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?){
        Log.i(TAG, "dump ${fd}.${writer}.${args}")
    }

    fun <T:Controller> getController(name:Class<T>) :T = Controller.getController(name)

    fun getContext()=mContext

    fun checkThread(){
        if(getHandler().looper.isCurrentThread){
            throw IllegalAccessError("not run in ${getHandler().looper.thread.name}")
        }
    }

    companion object {
        private val TAG = "${NubiaSystemUIApplication.TAG}.Controller"
        private val mMap = mutableMapOf<Class<*>, Controller>()
        fun forEach(action: (Controller) -> Unit) = mMap.values.forEach{ action.invoke(it) }

        fun init(context:NubiaSystemUIApplication){
            mMap[FingerprintController::class.java] = FingerprintController(context)
            mMap[FingerprintWindowController::class.java] = FingerprintWindowController(context)
        }

        fun <T:Controller> getController(name:Class<T>):T =  if(mMap.containsKey(name)){
            mMap[name] as T
        }else{
            throw IncompatibleClassChangeError("not find ${name}")
        }
    }
}