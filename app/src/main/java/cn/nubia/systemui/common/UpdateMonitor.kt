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
    private val mDisplayStateMap = mutableMapOf<Int, Int>()
    interface UpdateMonitorCallback{
        fun onSystemUIConnect(systemui: SystemUI){}
        fun onSystemUIDisConnect(){}
        fun onSystemUIChange(type:Int, data:Bundle){}
        fun onDisplayChange(displayId: Int, state: Int){}
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
                it.onSystemUIChange(type, data)
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

    fun callDisplayChange(displayId:Int, state:Int){
        if(!(mDisplayStateMap.containsKey(displayId) && mDisplayStateMap.get(displayId)==state)){
            mDisplayStateMap.put(displayId, state)
            mHandler.post{
                mList.forEach{
                    it.onDisplayChange(displayId, state)
                }
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