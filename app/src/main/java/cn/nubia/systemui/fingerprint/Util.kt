package cn.nubia.systemui.fingerprint

import android.util.Log
import cn.nubia.systemui.NubiaSystemUIApplication
import java.io.FileDescriptor
import java.io.PrintWriter
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

private val TAG = "${NubiaSystemUIApplication.TAG}.Util"
private var mAodMode:AtomicInteger = AtomicInteger(0)
private var mIsHbm:AtomicBoolean = AtomicBoolean(false)

@Synchronized fun setAodMode(mode:Int){
    if(mAodMode.get() != mode){
        mAodMode.getAndSet(mode)
    }
}

@Synchronized fun setHBM(enable:Boolean){
    if(mIsHbm.get() != enable){
        mIsHbm.getAndSet(enable)
    }
}

@Synchronized fun writeNode(path:String, value:String){
    Log.i(TAG,"writeNode ${path}:${value}")
}

fun dump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?) {
    Log.i(TAG,"dump ${fd}.${writer}.${args}")
}