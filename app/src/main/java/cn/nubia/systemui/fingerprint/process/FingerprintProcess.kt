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
import cn.nubia.systemui.fingerprint.FingerprintWindowController
import cn.nubia.systemui.common.setHBM
import java.io.FileDescriptor
import java.io.PrintWriter

abstract class  FingerprintProcess(val mContext:Context, val mFpController:FingerprintController,
                                   val mWindowController: FingerprintWindowController){
    val TAG by lazy { "${NubiaSystemUIApplication.TAG}.${this.javaClass.simpleName}"}
    val mThreadHelper = NubiaThreadHelper.get()
    val mFpManager :FingerprintManager = mContext.getSystemService(FingerprintManager::class.java)

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

    open fun onAcquired(info: Int) {
        if(info in FingerprintInfo) {
            if (FingerprintInfo.isVibrateError(info)){
                mFpController.vibrator()
            }
            when(info){

            }
        }
    }

    fun callFingerDown() {
        when (mState) {
            ProcessState.NORMAL,ProcessState.UPING -> {
                //待处理项
                mState = ProcessState.DOWNING
            }
            else -> {
                Log.w(TAG, "onTouchDown, but current state = ${mState}")
            }
        }
        onFingerDown()
    }

    open fun onFingerDown(){

        when (mState) {
            ProcessState.DOWNING -> {
                mWindowController.syn {
                    showFingerDownImage()
                }

                mFpController.mActionList.addHbmAction(ActionList.Action("down"){
                    Log.w(TAG, "hbm")
                })

                mThreadHelper.handlerBackground {
                    mFpManager.processCmd(BiometricCmd.CMD_DOWN, 0, 0, byteArrayOf(), 0)
                    setHBM(true)
                    mFpController.syn {
                        if (mState == ProcessState.DOWNING) {
                            mState = ProcessState.DOWN
                            onHbmEnable(true)
                            callUiReady()
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
            ProcessState.DOWN  ->{
                //待处理项
                mState = ProcessState.UI_READYING
            }
            else -> {
                Log.w(TAG, "callUiReady, but current state = ${mState}")
            }
        }
        onUiReady()
    }

    open fun onUiReady(){
        when(mState){
            ProcessState.UI_READYING -> {
                mThreadHelper.handlerBackground {
                    mFpManager.processCmd(BiometricCmd.CMD_UI_READY, 0, 0 , byteArrayOf(), 0)
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

    fun callFingerUp(){
        when(mState){
            ProcessState.DOWNING, ProcessState.DOWN  ->{
                //待处理项
                Log.i(TAG, "callFingerUp, but current state = ${mState}")
                mState = ProcessState.UPING
            }
            ProcessState.UI_READYING, ProcessState.UI_READY  ->{
                //待处理项
                Log.i(TAG, "callFingerUp, but current state = ${mState}")
                mState = ProcessState.UPING
            }
            else -> {
                Log.w(TAG, "callFingerUp, but current state = ${mState}")
            }
        }
        onFingerUp();
    }

    open fun onFingerUp(){
        when(mState){
            ProcessState.UPING -> {

                mWindowController.syn{
                    showFingerUpImage()
                }
                mThreadHelper.handlerBackground {
                    mFpManager.processCmd(BiometricCmd.CMD_UP, 0, 0 , byteArrayOf(), 0)
                    setHBM(false)
                    mFpController.syn{
                        if(mState == ProcessState.UPING){
                            onHbmEnable(false)
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
    open fun onIconShow() { }
    open fun onIconHide() { }
    open fun onDoneAuth() { }
    open fun onAuthError() { }
    open fun onFailAuth() { }
    open fun dump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?){

    }
}