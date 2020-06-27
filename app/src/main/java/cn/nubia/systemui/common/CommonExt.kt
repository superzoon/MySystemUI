package cn.nubia.systemui.common

import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import android.view.View
import cn.nubia.systemui.NubiaSystemUIApplication
import cn.nubia.systemui.NubiaSystemUIApplication.Companion.TAG
import cn.nubia.systemui.NubiaThreadHelper
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
private val mMonitorFileMap  = mutableMapOf<String, MutableList<(String?)->Unit>>()
private val mMonitorValueMap  = mutableMapOf<String, String>()

fun File.writeLine(str:String):Boolean = if(canWrite()){
        try {
            val w = FileWriter(this)
            w.write(str)
            w.flush()
            w.close()
            true
        }catch (e:Exception){
            Log.w(TAG, "ERROR write err:${e.message}")
            false
        }
    }else{
        Log.w(TAG, "ERROR not write:${this}")
        false
    }

fun File.readLine():String = if(canRead()){
        var r:BufferedReader? = null
        try {
            r = BufferedReader(FileReader(this))
            r.readLine()
        }catch (e:Exception){
            Log.w(TAG, "ERROR read err:${e.message}")
            ""
        } finally {
            r?.close()
        }
    }else{
        Log.w(TAG, "ERROR not read:${this}")
        ""
    }

fun File.unMonitor(callback:(String?)->Unit){
    NubiaThreadHelper.get().handlerFpBg {
        if(mMonitorFileMap.containsKey(name)){
            mMonitorFileMap[name]?.remove(callback)
        }
        if (mMonitorFileMap[name]?.size==0){
            mMonitorFileMap.remove(name)
            mMonitorValueMap.remove(name)
        }
    }
}

//读取阻塞节点文件，在后台线程返回
fun File.monitor(callback:(String?)->Unit){
    NubiaThreadHelper.get().handlerFpBg {
        if(mMonitorFileMap.containsKey(name)){
            mMonitorFileMap[name]?.add(callback)
            if(mMonitorValueMap[name]!=null){
                callback.invoke(mMonitorValueMap[name])
            }
        }else{
            mMonitorFileMap[name] = mutableListOf<(String?)->Unit>()
            Thread{
                mMonitorFileMap[name]?.apply {
                    add(callback)
                    var r: BufferedReader? = null
                    try {
                        if(canRead()){
                            r = BufferedReader(FileReader(name))
                            while (size>0){
                                mMonitorValueMap[name] = r.readLine()
                                if(!mMonitorValueMap[name].isNullOrEmpty()){
                                    NubiaThreadHelper.get().synFpBg {
                                        forEach {
                                            it.invoke(mMonitorValueMap[name])
                                        }
                                    }
                                }
                            }
                        }
                    }catch (e:Exception){
                        Log.w(NubiaSystemUIApplication.TAG, "ERROR read err:${e.message}")
                    } finally {
                        try {
                            r?.close()
                        }catch (e:Exception){}
                        NubiaThreadHelper.get().synFpBg {
                            forEach {
                                it.invoke(null)
                            }
                            Log.i(NubiaSystemUIApplication.TAG, "read end:${path}")
                        }
                    }
                }
            }.start()
        }
    }
}

/*
    扩展点击事件
 */
fun View.onClick(listener:View.OnClickListener):View{
    setOnClickListener(listener)
    return this
}

/*
    扩展点击事件，参数为方法
 */
fun View.onClick(method:() -> Unit):View{
    setOnClickListener { method() }
    return this
}
/*
    扩展视图可见性
 */
fun View.setVisible(visible:Boolean){
    this.visibility = if (visible) View.VISIBLE else View.GONE
}

fun PowerManager.setAODMode(token:IBinder, aodName:String, aodMode:Int, reason:String){
    //系统方法
}
/**
<< shl
>> shr
| or
& and
表达式	 对应的函数
a+b	     a.plus(b)
a-b	     a.minus(b)
a*b	     a.tims(b)
a/b	     a.div(b)
a%b      a.mod(b)
a.b    	 a.rangeTo(b)
a in b	 b.contains(a)
a !in b	!b.contains(a)
*/