package cn.nubia.systemui.fingerprint

import android.content.Context
import android.graphics.PointF
import android.os.Build
import android.os.SystemProperties
import android.os.Trace
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import cn.nubia.systemui.NubiaSystemUIApplication
import cn.nubia.systemui.NubiaThreadHelper
import cn.nubia.systemui.common.UpdateMonitor
import cn.nubia.systemui.common.readLine
import cn.nubia.systemui.common.writeLine
import java.io.File
import java.io.FileDescriptor
import java.io.PrintWriter
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

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
    File(HBM_MODE_PATH).writeLine("${if (enable) 1 else 0}")
    return enable
}

fun initHBM():Boolean = setHBM(File(HBM_MODE_PATH).readLine() == "1")

@Synchronized fun writeNode(path:String, value:String){
    File(path).writeLine(value)
}

fun getTimeStr():String = DATE_FORMAT.format(Date())

data class InfoStr(val mInfo:String, val mTime:String = getTimeStr())

fun traceLog(log:String){
    Trace.beginSection("${NubiaSystemUIApplication.TAG}.${log}")
    Log.i(NubiaSystemUIApplication.TAG,"traceLog log=${log}")
    Trace.endSection()
    Build.BOARD
}

fun isSupportFpWakeup():Boolean = SystemProperties.getInt("sys.nubia.fpmopde.private", 0) == 1
