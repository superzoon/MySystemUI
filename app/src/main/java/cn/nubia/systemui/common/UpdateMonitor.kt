package cn.nubia.systemui.ext

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import cn.nubia.systemui.aidl.ISystemUI
import cn.nubia.systemui.common.SystemUI

class UpdateMonitor private constructor(){
    private val mHandler = Handler(Looper.getMainLooper());
    private val mList = mutableListOf<UpdateMonitorCallback>()
    interface UpdateMonitorCallback{
        fun onSystemUIConnect(systemui: SystemUI){}
        fun onSystemUIDisConnect(){}
        fun callSystemUIChange(type:Int, data:Bundle){}
    }

    fun callSystemUIDisConnect(){
        mHandler.post{
            mList.forEach{
                it.onSystemUIDisConnect()
            }
        }
    }

    fun callSystemUIChange(type:Int, data:Bundle){
        mHandler.post{
            mList.forEach{
                it.callSystemUIChange(type, data)
            }
        }
    }

    fun callSystemUIConnect(systemui: SystemUI){
        mHandler.post{
            mList.forEach{
                it.onSystemUIConnect(systemui)
            }
        }
    }

    fun addCallback(callback: UpdateMonitorCallback){
        mHandler.post{
            if(!mList.contains(callback)){
                mList.add(callback)
            }
        }
    }

    fun removeCallback(callback: UpdateMonitorCallback){
        mHandler.post{
            if(mList.contains(callback)){
                mList.remove(callback)
            }
        }
    }

    companion object {
        private  var mUpdateMonitor:UpdateMonitor? = null
            get(){
                if (field == null){
                    field = UpdateMonitor()
                }
                return field
            }

        public fun get():UpdateMonitor{
            return mUpdateMonitor!!
        }
    }
}