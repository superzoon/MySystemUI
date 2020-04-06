package cn.nubia.systemui.fingerprint

import android.content.Context
import cn.nubia.systemui.common.SystemUI
import cn.nubia.systemui.ext.Controller
import cn.nubia.systemui.ext.UpdateMonitor
import cn.nubia.systemui.ext.UpdateMonitor.UpdateMonitorCallback

class FingerprintController(mContext:Context):Controller(mContext){
    val mHandler = ThreadHelp.get().getFingerHander()
    var isConnection = false
    var mSystemUI:SystemUI? = null
    
    init {
        UpdateMonitor.get().addCallback(object :UpdateMonitorCallback {
            override fun onSystemUIConnect(systemui: SystemUI) {
                mHandler.post{
                    super.onSystemUIConnect(systemui)
                    this@FingerprintController.onSystemUIConnect(systemui)
                }
            }
        })
    }

    fun onSystemUIConnect(systemui: SystemUI) {
        mSystemUI = systemui
        isConnection = true
    }
}