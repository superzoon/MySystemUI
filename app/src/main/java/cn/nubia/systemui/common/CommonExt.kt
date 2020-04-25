package cn.nubia.systemui.common

import android.hardware.fingerprint.FingerprintManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import cn.nubia.systemui.NubiaSystemUIApplication.Companion.TAG
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter


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

fun FingerprintManager.writeNode(fileName:String, value:Int){

}


fun FingerprintManager.processCmd(cmd:Int, arr1:Int, arr2:Int, sendBuff:ByteArray, len:Int): ByteArray?{
    if(cmd in BiometricCmd){
        return null
    }else{
        throw IllegalAccessError("processCmd cmd=${cmd}")
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
/**
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