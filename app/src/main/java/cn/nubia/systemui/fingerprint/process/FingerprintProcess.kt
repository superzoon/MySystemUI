package cn.nubia.systemui.fingerprint.process

import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.SystemClock
import android.os.processCmd
import android.util.Log
import cn.nubia.systemui.NubiaSystemUIApplication
import cn.nubia.systemui.fingerprint.FingerprintController
import cn.nubia.systemui.NubiaThreadHelper
import cn.nubia.systemui.common.*
import cn.nubia.systemui.fingerprint.process.ActionList.ActionKey
import cn.nubia.systemui.fingerprint.FingerprintWindowController
import cn.nubia.systemui.fingerprint.setHBM
import java.io.File
import java.io.FileDescriptor
import java.io.PrintWriter

abstract class  FingerprintProcess(val mContext:Context, val mFingerprintController:FingerprintController,
                                   val mWindowController: FingerprintWindowController){
    val TAG by lazy { "${NubiaSystemUIApplication.TAG}.${this.javaClass.simpleName}"}
    val mThreadHelper = NubiaThreadHelper.get()
    val mFingerprintManager :FingerprintManager = mContext.getSystemService(FingerprintManager::class.java)

    enum class ProcessState{
        NORMAL, DOWNING, DOWN, UI_READYING, UI_READY, UPING, UP
    }

    private var _STATE_: ProcessState = ProcessState.NORMAL
    private var mStateChangeTime = SystemClock.elapsedRealtime()

    var mState
        get() = _STATE_
        private set(value) {
            if(_STATE_!=value){
                Log.i(TAG, "process state change")
                mStateChangeTime = SystemClock.elapsedRealtime()
                _STATE_=value
            }
        }

    fun onTouchDown() {
        when (mState) {
            ProcessState.UPING -> {
                //待处理项
                mState = ProcessState.UP
            }
            ProcessState.NORMAL, ProcessState.UP -> {
                mState = ProcessState.DOWNING

                mThreadHelper.synInvoke(mWindowController.mHandler) {
                    mWindowController.showFingerDownImage()
                }

                mFingerprintController.mActionList.addHbmAction(ActionList.Action("down"){
                    Log.w(TAG, "hbm")
                })

                mThreadHelper.getBgHander().post {
                    mFingerprintManager.processCmd(BiometricCmd.CMD_DOWN, 0, 0, byteArrayOf(), 0)
                    setHBM(true)
                    mThreadHelper.synFingerprint {
                        if (mState == ProcessState.DOWNING) {
                            mFingerprintController.onHbmEnable(true)
                            mState = ProcessState.DOWN
                        }
                    }
                }
            }
            else -> {
                Log.w(TAG, "onTouchDown, but current state = ${mState}")
            }
        }
    }

    fun callUiReady(){
        when(mState){
            ProcessState.DOWNING  ->{
                //待处理项
                mState = ProcessState.UI_READY
            }
            ProcessState.DOWN -> {
                mState = ProcessState.UI_READYING
                mThreadHelper.getBgHander().post {
                    mFingerprintManager.processCmd(BiometricCmd.CMD_UI_READY, 0, 0 , byteArrayOf(), 0)
                    mThreadHelper.synFingerprint{
                        if(mState == ProcessState.UI_READYING){
                            mState = ProcessState.UI_READY
                        }
                    }
                }
            }
            else -> {
                Log.w(TAG, "onUiReady, but current state = ${mState}")
            }
        }
    }

    fun onTouchUp(){
        when(mState){
            ProcessState.UI_READYING  ->{
                //待处理项
                mState = ProcessState.UPING
            }
            ProcessState.UI_READY -> {
                mState = ProcessState.UPING

                mThreadHelper.synInvoke(mWindowController.mHandler){
                    mWindowController.showFingerUpImage()
                }
                mThreadHelper.getBgHander().post {
                    mFingerprintManager.processCmd(BiometricCmd.CMD_UP, 0, 0 , byteArrayOf(), 0)
                    setHBM(false)
                    mThreadHelper.synFingerprint{
                        if(mState == ProcessState.UPING){
                            mFingerprintController.onHbmEnable(false)
                            mState = ProcessState.UP
                        }
                    }
                }
            }
            else -> {
                Log.w(TAG, "onTouchUp, but current state = ${mState}")
            }
        }
    }

    open fun onAcquired(info: Int) {
        when(info){
            in FingerprintInfo -> {
                if (FingerprintInfo.isVibrateError(info)){

                }
            }
        }
    }

    open fun onIconShow() {}
    open fun onIconHide() {}
    open fun onStartAuth(owner: String?) {
        mWindowController.show()
    }
    open fun onDoneAuth() { }
    open fun onAuthError() { }
    open fun onFailAuth() { }
    open fun onStopAuth() { }
    abstract fun dump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?)
}