package cn.nubia.systemui.ext

import android.content.Context
import android.os.Handler
import android.os.Looper
import cn.nubia.systemui.aidl.ISystemUI
import cn.nubia.systemui.common.SystemUI

class UpdateMonitor private constructor(){
    private val mHandler = Handler(Looper.getMainLooper());
    val mList = mutableListOf<UpdateMonitorCallback>()
    interface UpdateMonitorCallback{
        fun onSystemUIConnect(systemui: SystemUI){}
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