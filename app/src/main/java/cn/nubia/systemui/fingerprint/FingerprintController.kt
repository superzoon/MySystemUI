package cn.nubia.systemui.fingerprint

import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.Choreographer
import android.view.Display
import cn.nubia.systemui.fingerprint.process.ActionList.ActionKey
import cn.nubia.systemui.NubiaSystemUIApplication
import cn.nubia.systemui.NubiaSystemUIService
import cn.nubia.systemui.NubiaThreadHelper
import cn.nubia.systemui.common.SystemUI
import cn.nubia.systemui.common.Controller
import cn.nubia.systemui.common.Dump
import cn.nubia.systemui.common.UpdateMonitor
import cn.nubia.systemui.fingerprint.process.*
import java.io.FileDescriptor
import java.io.PrintWriter


class FingerprintController(mContext:Context):Controller(mContext), Dump {

    companion object {
        val TAG = "${NubiaSystemUIApplication.TAG}.FingerprintController"
    }

    private val mHandler = NubiaThreadHelper.get().getFingerHander()

    private val mWindowController by lazy { getController(FingerprintWindowController::class.java) }

    val mActionList = ActionList()
    private var isConnection = false
    private var mSystemUI: SystemUI? = null
    private var mDisplayState: Int = Display.STATE_UNKNOWN
    private var mDisplayStateStr: String = "STATE_UNKNOWN"
    private var mOldProcess: FingerprintProcess? = null
    private var mCurrentProcess: FingerprintProcess? = null

    private val mChoreographer by lazy {
        if (Thread.currentThread() == mHandler.looper.thread) {

            Choreographer.getInstance()
        } else {

            NubiaThreadHelper.get().synFingerprintInvoke{
                    Choreographer.getInstance()
                }!!
        }
    }

    fun postFrameDelayed(run:(frameTimeNanos:Long)->Unit, delay:Long){
        mChoreographer.postFrameCallbackDelayed(run, delay)
    }
    private val mMonitor = object : NubiaBiometricMonitor.UpdateMonitorCallback,
            UpdateMonitor.UpdateMonitorCallback, FingerprintWindowController.Callback {
        override fun onShow() {
            mHandler.post {
                this@FingerprintController.onIconShow()
            }
        }

        override fun onHide() {
            mHandler.post {
                this@FingerprintController.onIconHide()
            }
        }

        override fun onDown() {
            mHandler.post {
                this@FingerprintController.onFingerprintDown()
            }
        }

        override fun onUp() {
            mHandler.post {
                this@FingerprintController.onFingerprintUp()
            }
        }

        val DEBUG = false

        fun log(msg: Any) {
            if (DEBUG) Log.i(TAG, "${msg}")
        }

        override fun onStartAuth(owner: String?) {
            mHandler.post {
                this@FingerprintController.onStartAuth(owner)
            }
        }

        override fun onDoneAuth() {
            mHandler.post {
                this@FingerprintController.onDoneAuth()
            }
        }

        override fun onStopAuth() {
            mHandler.post {
                this@FingerprintController.onStopAuth()
            }
        }

        override fun onFailAuth() {
            mHandler.post {
                this@FingerprintController.onFailAuth()
            }
        }

        override fun onAuthError() {
            mHandler.post {
                this@FingerprintController.onAuthError()
            }
        }

        override fun onAcquired(info: Int) {
            mHandler.post {
                this@FingerprintController.onAcquired(info)
            }
        }


        override fun onSystemUIDisConnect() {
            super.onSystemUIDisConnect()
            mHandler.post {
                this@FingerprintController.onSystemUIDisConnect()
            }
        }

        override fun onSystemUIConnect(systemui: SystemUI) {
            super.onSystemUIConnect(systemui)
            log("onSystemUIConnect systemui=${systemui}")
            mHandler.post {
                this@FingerprintController.onSystemUIConnect(systemui)
            }
        }

        override fun onDisplayChange(displayId: Int, state: Int, stateStr: String) {
            super.onDisplayChange(displayId, state, stateStr)
            log("onDisplayChange displayId=${displayId} ${state} ${stateStr}")
            mHandler.post {
                this@FingerprintController.onDisplayChange(displayId, state, stateStr)
            }
        }

        override fun onFingerprintKeycode(keycode: Int) {
            log("onFingerprintKeycode keycode=${keycode}")
        }

    }

    override fun getHandler(): Handler {
        return mHandler
    }

    private fun onDisplayChange(displayId: Int, state: Int, stateStr: String) {
        if (displayId == Display.DEFAULT_DISPLAY) {
            mDisplayState = state
            mDisplayStateStr = stateStr
            if(mDisplayState in ActionKey){
                mActionList[mDisplayState].removeAll {
                    it.invoke()
                }
            }else{
                Log.w(TAG,"ERROR state ${stateStr}")
            }
        }
    }

    private fun onSystemUIDisConnect() {
        mSystemUI = null
        isConnection = false
    }

    private fun onSystemUIConnect(systemui: SystemUI) {
        mSystemUI = systemui
        isConnection = true
    }

    override fun onStart(service: NubiaSystemUIService) {
        UpdateMonitor.get().addCallback(mMonitor)
        NubiaBiometricMonitor.get().addCallback(mMonitor)
    }

    override fun onStop(service: NubiaSystemUIService) {
        UpdateMonitor.get().removeCallback(mMonitor)
        NubiaBiometricMonitor.get().removeCallback(mMonitor)
    }

    fun onFingerprintDown() {
        when (mDisplayState) {
            Display.STATE_OFF -> {
                mCurrentProcess = ScreenOffProcess(mContext,this)
            }
            Display.STATE_ON -> {
                mCurrentProcess = ScreenOnProcess(mContext,this)
            }
            Display.STATE_DOZE, Display.STATE_DOZE_SUSPEND -> {
                mCurrentProcess = ScreenAodProcess(mContext,this)
            }
            else -> {
                mCurrentProcess = null
            }
        }
        mCurrentProcess?.onTouchDown()
    }

    fun onFingerprintUp() {
        mCurrentProcess?.onTouchUp()
        mOldProcess = mCurrentProcess
        mCurrentProcess = null
    }

    override fun dump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?) {
        mOldProcess?.dump(fd, writer, args)
        mCurrentProcess?.dump(fd, writer, args)
    }

    private fun onIconShow() {
        mCurrentProcess?.onIconShow()
    }

    private fun onIconHide() {
        mCurrentProcess?.onIconHide()
    }

    private fun onStartAuth(owner: String?) {
        mCurrentProcess?.onStartAuth(owner)
    }

    private fun onDoneAuth() {
        mCurrentProcess?.onDoneAuth()
    }

    private fun onAcquired(info: Int) {
        mCurrentProcess?.onAcquired(info)
    }

    private fun onAuthError() {
        mCurrentProcess?.onAuthError()
    }

    private fun onFailAuth() {
        mCurrentProcess?.onFailAuth()
    }

    private fun onStopAuth() {
        mCurrentProcess?.onStopAuth()
    }

    fun addHbmAction(flowAction: Action) {
        mActionList[ActionKey.KEY_SCREEN_HBM].add(flowAction)
    }

    fun onHbmEnable(enbale: Boolean) {
        if(enbale){
            mActionList[ActionKey.KEY_SCREEN_HBM].removeAll{
                it.invoke()
            }
        }
    }

}



