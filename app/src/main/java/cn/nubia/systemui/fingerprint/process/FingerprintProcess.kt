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
import cn.nubia.systemui.fingerprint.action.ActionEvent
import java.io.FileDescriptor
import java.io.PrintWriter

abstract class  FingerprintProcess(val mContext:Context, val mFpController:FingerprintController,
                                   val mWindowController: FingerprintWindowController){
    companion object {
        val STATE_AUTH_FINGER_UP = 1
        val STATE_AUTH_DONE = 0
        val STATE_AUTH_ERROR = -1
        val STATE_AUTH_FAIL = -2
    }
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

    fun delay(delay:Long, actin:()->Unit){
        mFpController.getHandler().postDelayed(actin, delay)
    }

    fun callFingerDown() {
        when (mState) {
            ProcessState.NORMAL,ProcessState.UPING -> {
                //待处理项
                mState = ProcessState.DOWNING
                mFpController.mActionEvent.addFingerDown(ActionEvent.Action("onFingerDown"){
                    Log.w(TAG, "onFingerDown")
                    onFingerDown()
                })
                triggerFingerDown()
            }
            else -> {
                Log.w(TAG, "onTouchDown, but current state = ${mState}")
            }
        }
    }

    open protected fun triggerFingerDown(){
        if(mState == ProcessState.DOWNING){
            delay(getFingerDownDelay()){
                mFpController.onFingerDown()
            }
        }
    }

    open protected fun onFingerDown(){

        when (mState) {
            ProcessState.DOWNING -> {
                mWindowController.syn {
                    showFingerDownImage()
                }

                mFpController.mActionEvent.addHbmAction(ActionEvent.Action("down"){
                    Log.w(TAG, "hbm")
                })

                mThreadHelper.handlerFpBg {
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
                mFpController.mActionEvent.addFingerUIReady(ActionEvent.Action("onFingerUIReady"){
                    Log.w(TAG, "onFingerUIReady")
                    onUIReady()
                })
                triggerFingerUIReadyDelay()
            }
            else -> {
                Log.w(TAG, "callUiReady, but current state = ${mState}")
            }
        }
    }

    open protected fun triggerFingerUIReadyDelay(){
        if(mState == ProcessState.UI_READYING){
            delay(getFingerUIReadyDelay()){
                mFpController.onFingerUIReady()
            }
        }
    }

    open protected fun onUIReady(){
        when(mState){
            ProcessState.UI_READYING -> {
                mThreadHelper.handlerFpBg {
                    mFpManager.processCmd(BiometricCmd.CMD_UI_READY, 0, 0 , byteArrayOf(), 0)
                    mThreadHelper.synFpFront{
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
            ProcessState.DOWNING->{
                Log.i(TAG, "callFingerUp, but current state = ${mState}")
            }
            ProcessState.DOWN, ProcessState.UI_READYING ->{
                //待处理项
                Log.i(TAG, "callFingerUp, but current state = ${mState}")
                mState = ProcessState.UPING
                mFpController.mActionEvent.clearAllAction()
                mFpController.onFingerUp()
            }
            ProcessState.UI_READY  ->{
                //待处理项
                Log.i(TAG, "callFingerUp, but current state = ${mState}")
                mState = ProcessState.UPING
                mFpController.mActionEvent.addFingerUp(ActionEvent.Action("onFingerUp"){
                    Log.w(TAG, "onFingerUp")
                    onFingerUp();
                })
                triggerFingerUp()
            }
            else -> {
                Log.w(TAG, "callFingerUp, but current state = ${mState}")
            }
        }
    }
    open protected fun triggerFingerUp(){
        if(mState == ProcessState.UPING){
            delay(getFingerUpDelay()){
                mFpController.onFingerUp()
            }
        }
    }
    open protected fun onFingerUp(){
        when(mState){
            ProcessState.UPING -> {
                onAuthStateChange(STATE_AUTH_FINGER_UP);
                mThreadHelper.handlerFpBg {
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
    abstract fun getFingerDownDelay():Long
    abstract fun getFingerUIReadyDelay():Long
    abstract fun getFingerUpDelay():Long
    open fun onIconShow() { }
    open fun onIconHide() { }
    open fun onDoneAuth() {
        onAuthStateChange(STATE_AUTH_DONE);
    }
    open fun onAuthError() {
        onAuthStateChange(STATE_AUTH_ERROR);
    }
    open fun onFailAuth() {
        onAuthStateChange(STATE_AUTH_FAIL);
    }
    private fun onAuthStateChange(state:Int){
        mWindowController.post{
            showFingerUpImage()
        }
        when(state){
            STATE_AUTH_FINGER_UP, STATE_AUTH_DONE ->{

            }
            STATE_AUTH_ERROR, STATE_AUTH_FAIL ->{

            }
        }
    }
    open fun dump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?){

    }
}