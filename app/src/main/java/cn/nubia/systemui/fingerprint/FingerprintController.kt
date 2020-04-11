package cn.nubia.systemui.fingerprint

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Choreographer
import android.view.Display
import cn.nubia.systemui.NubiaSystemUIApplication
import cn.nubia.systemui.NubiaSystemUIService
import cn.nubia.systemui.common.SystemUI
import cn.nubia.systemui.common.Controller
import cn.nubia.systemui.common.UpdateMonitor
import cn.nubia.systemui.common.UpdateMonitor.UpdateMonitorCallback

class FingerprintController(mContext:Context):Controller(mContext){

    companion object {
        val TAG = "${NubiaSystemUIApplication.TAG}.FingerprintController"
    }

    private val mHandler = ThreadHelper.get().getFingerHander()

    private val mWindowController by lazy { getController(FingerprintWindowController::class.java) }

    private var isConnection = false
    private var mSystemUI:SystemUI? = null
    private var mDisplayDtate:Int = Display.STATE_UNKNOWN
    private var mFlow:FingerprintFlow? = null

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
    private val mCallback = object :UpdateMonitorCallback {

        override fun onSystemUIDisConnect() {
            super.onSystemUIDisConnect()
            mHandler.post{
                this@FingerprintController.onSystemUIDisConnect()
            }
        }

        override fun onSystemUIConnect(systemui: SystemUI) {
            super.onSystemUIConnect(systemui)
            Log.i(TAG,"onSystemUIConnect systemui=${systemui}")
            mHandler.post{
                this@FingerprintController.onSystemUIConnect(systemui)
            }
        }

        override fun onDisplayChange(displayId: Int, state: Int, stateStr:String) {
            super.onDisplayChange(displayId, state, stateStr)
            Log.i(TAG, "onDisplayChange displayId=${displayId} ${state} ${stateStr}")
            mHandler.post{
                this@FingerprintController.onDisplayChange(displayId, state, stateStr)
            }
        }

        override fun onSystemUIChange(type: Int, data: Bundle) {
            super.onSystemUIChange(type, data)
            Log.i(TAG,"onSystemUIChange type=${type} data=${data}")
            mHandler.post{
                this@FingerprintController.onSystemUIChange(type, data)
            }
        }
    }

    init {
        UpdateMonitor.get().addCallback(mCallback)
    }

    override fun getHandler(): Handler {
        return mHandler
    }

    private fun onSystemUIChange(type: Int, data: Bundle){
    }

    private fun onDisplayChange(displayId: Int, state: Int, stateStr:String){
        if(displayId == Display.DEFAULT_DISPLAY){
            mDisplayDtate = state;
        }
    }

    private fun onSystemUIDisConnect(){
        mSystemUI = null
        isConnection = false
    }

    private fun onSystemUIConnect(systemui: SystemUI) {
        mSystemUI = systemui
        isConnection = true
    }

    override fun onStart(service: NubiaSystemUIService) {

    }

    override fun onStop(service: NubiaSystemUIService) {

    }
}