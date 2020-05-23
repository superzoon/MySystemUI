package cn.nubia.systemui.common

import android.os.SystemClock
import java.awt.font.NumericShaper
import java.text.SimpleDateFormat
import java.util.*

class InfoLog (val mInfo:Any, private val mTime:Long=SystemClock.uptimeMillis()):Comparable<InfoLog>{
    companion object {
        private val FORMAT = SimpleDateFormat("HH:mm:ss.SSS")
        private val LIST = arrayOfNulls<InfoLog>(50)
        private var mPoint = 0;
        fun addLog(any:Any){
            LIST[mPoint] = InfoLog(any)
            mPoint++
            mPoint %= LIST.size
        }

        fun cleanLog(){
            for(index in IntRange(0, LIST.size)){
                LIST[index%LIST.size] = null
            }
            mPoint=0
        }

        fun getLogString():String{
            val buff = StringBuffer()
            for(index in IntRange(mPoint, mPoint+LIST.size)){
                (index%LIST.size).let {
                    if(LIST[it]!=null){
                        buff.append("${LIST[it]}\n")
                    }
                }
            }
            return buff.toString()
        }

        fun sort(infos:List<InfoLog>):List<InfoLog>{
            Collections.sort(infos)
            return infos
        }
    }

    override fun compareTo(other: InfoLog): Int = (mTime-other.getTime()).toInt()

    fun getTime():Long = mTime

    override fun toString(): String {
        return "${FORMAT.format(Date(mTime))} info=${mInfo}"
    }
}