package cn.nubia.systemui.fingerprint

import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.Display
import cn.nubia.systemui.fingerprint.process.ActionList.ActionKey
import cn.nubia.systemui.NubiaSystemUIApplication
import cn.nubia.systemui.NubiaSystemUIService
import cn.nubia.systemui.NubiaThreadHelper
import cn.nubia.systemui.common.*
import cn.nubia.systemui.fingerprint.process.*
import java.io.FileDescriptor
import java.io.PrintWriter


class FingerprintController(mContext:Context):Controller(mContext), DumpHelper.Dump {

    companion object {
        val TAG = "${NubiaSystemUIApplication.TAG}.FingerprintController"
    }

    private val mHandler = NubiaThreadHelper.get().getFingerHander()

    private val mWindowController by lazy { getController(FingerprintWindowController::class.java) }

    val mActionList = ActionList(this)
    private var isConnection = false
    private var isFingerDown = false
    private var mSystemUI: SystemUI? = null
    private var mDisplayState: Int = Display.STATE_UNKNOWN
    private var mDisplayStateStr: String = "STATE_UNKNOWN"
    private var mOldProcess: FingerprintProcess? = null
    private var mCurrentProcess: FingerprintProcess? = null

    private val mMonitor = object : NubiaBiometricMonitor.UpdateMonitorCallback,
            UpdateMonitor.UpdateMonitorCallback, FingerprintWindowController.Callback {
        override fun onShow() {
            handlerInvoke (this@FingerprintController::onIconShow)
        }

        override fun onHide() {
            handlerInvoke (this@FingerprintController::onIconHide)
        }

        override fun onFingerDown() {
            Log.i(TAG, "onFingerDown 3")
            handlerInvoke (this@FingerprintController::onFingerDown)
        }

        override fun onFingerUp() {
            Log.i(TAG, "onFingerUp 3")
            handlerInvoke (this@FingerprintController::onFingerUp)
        }

        val DEBUG = false

        fun log(msg: Any) {
            if (DEBUG) Log.i(TAG, "${msg}")
        }

        override fun onStartAuth(owner: String?) {
            handlerInvoke {
                this@FingerprintController.onStartAuth(owner)
            }
        }

        override fun onDoneAuth() {
            handlerInvoke (this@FingerprintController::onDoneAuth)
        }

        override fun onStopAuth() {
            handlerInvoke (this@FingerprintController::onStopAuth)
        }

        override fun onFailAuth() {
            handlerInvoke (this@FingerprintController::onFailAuth)
        }

        override fun onAuthError() {
            handlerInvoke (this@FingerprintController::onAuthError)
        }

        override fun onAcquired(info: Int) {
            handlerInvoke {
                this@FingerprintController.onAcquired(info)
            }
        }


        override fun onSystemUIDisConnect() {
            super.onSystemUIDisConnect()
            handlerInvoke (this@FingerprintController::onSystemUIDisConnect)
        }

        override fun onSystemUIConnect(systemui: SystemUI) {
            super.onSystemUIConnect(systemui)
            log("onSystemUIConnect systemui=${systemui}")
            handlerInvoke {
                this@FingerprintController.onSystemUIConnect(systemui)
            }
        }

        override fun onDisplayChange(displayId: Int, state: Int, stateStr: String) {
            super.onDisplayChange(displayId, state, stateStr)
            log("onDisplayChange displayId=${displayId} ${state} ${stateStr}")
            handlerInvoke {
                this@FingerprintController.onDisplayChange(displayId, state, stateStr)
            }
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
        handlerInvoke { mWindowController.mCallback=mMonitor }
        mDisplayState = UpdateMonitor.get().getDisplayState()
    }

    override fun onStop(service: NubiaSystemUIService) {
        UpdateMonitor.get().removeCallback(mMonitor)
        NubiaBiometricMonitor.get().removeCallback(mMonitor)
    }

    fun onFingerDown() {
        checkThread()
        Log.i(TAG, "onFingerDown 4 ${isFingerDown} ${mDisplayState}")
        if(!isFingerDown){
            isFingerDown = true
            when (mDisplayState) {
                Display.STATE_OFF -> {
                    mCurrentProcess = ScreenOffProcess(mContext,this, mWindowController)
                }
                Display.STATE_ON -> {
                    mCurrentProcess = ScreenOnProcess(mContext,this, mWindowController)
                }
                Display.STATE_DOZE, Display.STATE_DOZE_SUSPEND -> {
                    mCurrentProcess = ScreenAodProcess(mContext,this, mWindowController)
                }
                else -> {
                    mCurrentProcess = null
                }
            }
            mCurrentProcess?.callFingerDown()
        }
        Log.i(TAG, "onFingerDown ${mCurrentProcess} ${mCurrentProcess?.javaClass?.simpleName}")
    }

    fun onFingerUp() {
        checkThread()
        Log.i(TAG, "onFingerUp 3 ${isFingerDown} ${mCurrentProcess} ${mCurrentProcess?.javaClass?.simpleName}")
        if(isFingerDown){
            isFingerDown = false
            mCurrentProcess?.callFingerUp()
            mOldProcess = mCurrentProcess
            mCurrentProcess = null
        }
    }

    override fun dump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?) {
        writer?.apply {
            write("mOldProcess:")
            mOldProcess?.dump(fd, this, args)
            write("mCurrentProcess:")
            mCurrentProcess?.dump(fd, this, args)
        }
    }

    private fun onIconShow() {
        checkThread()
        mCurrentProcess?.onIconShow()
    }

    private fun onIconHide() {
        checkThread()
        mCurrentProcess?.onIconHide()
    }

    private fun onStartAuth(owner: String?) {
        checkThread()
        mWindowController.show()
    }

    private fun onDoneAuth() {
        checkThread()
        mCurrentProcess?.onDoneAuth()
    }

    private fun onAcquired(info: Int) {
        checkThread()
        mCurrentProcess?.onAcquired(info)
    }

    private fun onAuthError() {
        checkThread()
        mCurrentProcess?.onAuthError()
    }

    private fun onFailAuth() {
        checkThread()
        mCurrentProcess?.onFailAuth()
    }

    private fun onStopAuth() {
        checkThread()
        mWindowController.hide()
    }

    var isHbm = false
    fun onHbmEnable(enbale: Boolean) {
        checkThread()
        isHbm = enbale
        if(isHbm){
            mActionList.invoke(ActionKey.KEY_SCREEN_HBM)
        }
        mCurrentProcess?.callUiReady()
    }
}



