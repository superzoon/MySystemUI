package cn.nubia.systemui.fingerprint

import android.content.Context
import android.hardware.biometrics.IBiometricServiceReceiverInternal
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
    private var mDisplayStateStr:String = "STATE_UNKNOWN"
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

    private val mNubiaBiometricMonitor = object : NubiaBiometricMonitor.UpdateMonitorCallback{
        override fun onStartAuth(owner: String?) {
        }

        override fun onDoneAuth() {
        }

        override fun onStopAuth() {
        }

        override fun onFailAuth() {
        }

        override fun onAuthError() {
        }

        override fun onAcquired(info: Int) {
        }

    }

    private val mSystemBiometricMonitor = object : SystemBiometricMonitor.UpdateMonitorCallback{
        val DEBUG = false

        fun log(msg:Any){
            if(DEBUG) Log.i(TAG,"${msg}")
        }

        override fun showBiometricView(bundle: Bundle?, receiver: IBiometricServiceReceiverInternal?, type: Int, requireConfirmation: Boolean, userId: Boolean){
            log( "showBiometricView bundle=${bundle} receiver=${receiver} type=${type} requireConfirmation=${requireConfirmation} userId=${userId }")
        }

        override fun hideBiometricView(){
            log( "hideBiometricView")
        }

        override fun onBiometricAuthenticated(authenticated: Boolean, failureReason: String?){
            log( "onBiometricAuthenticated authenticated=${authenticated} failureReason=${failureReason}")
        }

        override fun onBiometricHelp(message: String?){
            log( "onBiometricHelp message=${message}")
        }

        override fun onBiometricError(error: String?){
            log( "onBiometricError error=${error}")
        }
    }

    private val mCallback = object :UpdateMonitor.UpdateMonitorCallback {
        val DEBUG = false

        fun log(msg:Any){
           if(DEBUG) Log.i(TAG,"${msg}")
        }

        override fun onSystemUIDisConnect() {
            super.onSystemUIDisConnect()
            mHandler.post{
                this@FingerprintController.onSystemUIDisConnect()
            }
        }

        override fun onSystemUIConnect(systemui: SystemUI) {
            super.onSystemUIConnect(systemui)
            log("onSystemUIConnect systemui=${systemui}")
            mHandler.post{
                this@FingerprintController.onSystemUIConnect(systemui)
            }
        }

        override fun onDisplayChange(displayId: Int, state: Int, stateStr:String) {
            super.onDisplayChange(displayId, state, stateStr)
            log( "onDisplayChange displayId=${displayId} ${state} ${stateStr}")
            mHandler.post{
                this@FingerprintController.onDisplayChange(displayId, state, stateStr)
            }
        }

        override fun onFingerprintKeycode(keycode:Int){
            log( "onFingerprintKeycode keycode=${keycode}")
        }

    }

    init {
        UpdateMonitor.get().addCallback(mCallback)
        NubiaBiometricMonitor.get().addCallback(mNubiaBiometricMonitor)
        SystemBiometricMonitor.get().addCallback(mSystemBiometricMonitor)
    }

    override fun getHandler(): Handler {
        return mHandler
    }

    private fun onDisplayChange(displayId: Int, state: Int, stateStr:String){
        if(displayId == Display.DEFAULT_DISPLAY){
            mDisplayState = state
            mDisplayStateStr = stateStr
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
        mFlow?.dump(fd, writer, args)
    }
}