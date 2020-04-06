package cn.nubia.systemui.fingerprint

import android.annotation.SuppressLint
import android.content.Context
import android.view.Choreographer
import cn.nubia.systemui.common.SystemUI
import cn.nubia.systemui.ext.Controller
import cn.nubia.systemui.ext.UpdateMonitor
import cn.nubia.systemui.ext.UpdateMonitor.UpdateMonitorCallback

@SuppressLint("NewApi")
class FingerprintController(mContext:Context):Controller(mContext){
    private val mHandler = ThreadHelper.get().getFingerHander()
    private val mWindowController by lazy { getController(FingerprintWindowController::class.java) }
    private var isConnection = false
    private var mSystemUI:SystemUI? = null
    private val mChoreographer by lazy {
        if(Thread.currentThread()==mHandler.looper.thread){
            Choreographer.getInstance()
        }else{
            ThreadHelper.get().synFingerprint(action = object :ThreadHelper.Action<Choreographer>{
                override fun action(): Choreographer {
                    return Choreographer.getInstance()
                }
            })
        }
    }

    private var mFlow:FingerprintFlow? = null

    init {
        UpdateMonitor.get().addCallback(object :UpdateMonitorCallback {
            override fun onSystemUIDisConnect() {
                mHandler.post{
                    super.onSystemUIDisConnect()
                    this@FingerprintController.onSystemUIDisConnect()
                }
            }

            override fun onSystemUIConnect(systemui: SystemUI) {
                mHandler.post{
                    super.onSystemUIConnect(systemui)
                    this@FingerprintController.onSystemUIConnect(systemui)
                }
            }
        })
    }

    private fun onSystemUIDisConnect(){
        mSystemUI = null
        isConnection = false
    }

    private fun onSystemUIConnect(systemui: SystemUI) {
        mSystemUI = systemui
        isConnection = true
    }
}