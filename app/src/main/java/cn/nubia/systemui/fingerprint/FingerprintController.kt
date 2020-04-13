package cn.nubia.systemui.fingerprint

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
import cn.nubia.systemui.common.Dump
import cn.nubia.systemui.common.UpdateMonitor
import cn.nubia.systemui.common.UpdateMonitor.UpdateMonitorCallback
import cn.nubia.systemui.fingerprint.flow.FingerprintFlow
import cn.nubia.systemui.fingerprint.flow.FlowAction
import java.io.FileDescriptor
import java.io.PrintWriter

class FingerprintController(mContext:Context):Controller(mContext), Dump{

    companion object {
        val TAG = "${NubiaSystemUIApplication.TAG}.FingerprintController"
        val KEY_FLOW_SCREEN_OFF = Display.STATE_OFF
        val KEY_FLOW_SCREEN_ON = Display.STATE_ON
        val KEY_FLOW_SCREEN_AOD = Display.STATE_DOZE
        val KEY_FLOW_SCREEN_HBM = 1.shl(4)
    }

    private val mHandler = ThreadHelper.get().getFingerHander()

    private val mWindowController by lazy { getController(FingerprintWindowController::class.java) }

    private val mFlowAction:Map<Int, MutableList<FlowAction>> = mapOf(
            KEY_FLOW_SCREEN_OFF to mutableListOf<FlowAction>(),
            KEY_FLOW_SCREEN_ON to mutableListOf<FlowAction>(),
            KEY_FLOW_SCREEN_AOD to mutableListOf<FlowAction>(),
            KEY_FLOW_SCREEN_HBM to mutableListOf<FlowAction>()
    )

    private var isConnection = false
    private var mSystemUI:SystemUI? = null
    private var mDisplayState:Int = Display.STATE_UNKNOWN
    private var mFlow: FingerprintFlow? = null

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
            mDisplayState = state;
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

    fun onFingerprintDown(){
        when(mDisplayState){
            Display.STATE_OFF -> {
                mFlow = FingerprintFlow.ScreenOffFlow(this)
            }
            Display.STATE_ON -> {
                mFlow = FingerprintFlow.ScreenOnFlow(this)
            }
            Display.STATE_DOZE , Display.STATE_DOZE_SUSPEND -> {
                mFlow = FingerprintFlow.ScreenAodFlow(this)
            }
            else -> { mFlow = null }
        }
        mFlow?.callDown()
    }

    fun onFingerprintUp(){
        mFlow?.callUp()
    }

    override fun dump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?) {
        super.dump(fd, writer, args)
        mFlow?.dump(fd, writer, args)
    }
}