package cn.nubia.systemui.common

import android.os.Trace
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import cn.nubia.systemui.NubiaSystemUIApplication
import cn.nubia.systemui.NubiaThreadHelper
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

private val TAG = "${NubiaSystemUIApplication.TAG}.Util"

val TP_MODE_PATH = "/sys/kernel/tp_node/fp_mode"
val HBM_MODE_PATH = "/sys/kernel/lcd_enhance/hbm_mode"

private val DATE_FORMAT:DateFormat = SimpleDateFormat("HH:mm:ss.SSS")

private var mIsHbm:Boolean = File(HBM_MODE_PATH).readLine() == "1"

private val mVibrator by lazy {
    NubiaSystemUIApplication.getContext().getSystemService(Vibrator::class.java);
}

fun vibrator(milliseconds:Long, amplitude:Int){
    NubiaThreadHelper.get().getBgHander().post {
        mVibrator.vibrate(VibrationEffect.createOneShot(milliseconds, amplitude))
    }
}

@Synchronized fun setAodMode(mode:Int){
    UpdateMonitor.get().getSystemUI()?.setAodMode(mode)
}

@Synchronized fun setTpMode(mode:Int){
    File(TP_MODE_PATH).writeLine("${mode}")
}

@Synchronized fun setHBM(enable:Boolean):Boolean{
    if(mIsHbm != enable){
        mIsHbm = enable
    }
    traceStart("hbm")
    File(HBM_MODE_PATH).writeLine("${if (enable) 1 else 0}")
    traceEnd("hbm")
    return enable
}

fun initHBM():Boolean = setHBM(File(HBM_MODE_PATH).readLine() == "1")

@Synchronized fun writeNode(path:String, value:String){
    File(path).writeLine(value)
}

fun getTimeStr():String = DATE_FORMAT.format(Date())

data class InfoStr(val mInfo:String, val mTime:String = getTimeStr())

val mTracesList = mutableListOf<String>()

fun traceStart(target:String){
    if(target !in mTracesList){
        mTracesList.add(target)
        Trace.beginSection("${NubiaSystemUIApplication.TAG}.${target}")
        Log.i(NubiaSystemUIApplication.TAG,"traceLog start target=${target}")
    }else{
        Log.w(NubiaSystemUIApplication.TAG,"trace start err target=${target}")
        Log.w(NubiaSystemUIApplication.TAG,"has target=${target}, so return")
    }
    Trace.endSection()
}

fun traceEnd(target:String){
    if(target in mTracesList){
        mTracesList.remove(target)
        Trace.endSection()
        Log.i(NubiaSystemUIApplication.TAG,"traceLog end target=${target}")
    }else{
        Log.w(NubiaSystemUIApplication.TAG,"trace end err target=${target}")
    }
}

