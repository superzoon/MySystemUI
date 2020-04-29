package cn.nubia.systemui.fingerprint.process

import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.util.Log
import cn.nubia.systemui.NubiaSystemUIApplication
import cn.nubia.systemui.fingerprint.FingerprintController
import cn.nubia.systemui.NubiaThreadHelper
import cn.nubia.systemui.fingerprint.process.ActionList.ActionKey
import cn.nubia.systemui.common.BiometricCmd
import cn.nubia.systemui.common.FingerprintInfo
import cn.nubia.systemui.common.processCmd
import cn.nubia.systemui.fingerprint.FingerprintWindowController
import cn.nubia.systemui.fingerprint.setHBM
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

    private var mState: ProcessState = ProcessState.NORMAL
        get() = field
        set(value) {
            if(field!=value){
                Log.i(TAG, "process state change")
                field=value
            }
        }

    fun onTouchDown() {
        when(mState){
            ProcessState.UPING  ->{

                mState = ProcessState.UP
            }
            ProcessState.NORMAL ,ProcessState.UP  -> {
                mState = ProcessState.DOWNING

                mFingerprintController.mActionList.addHbmAction(object : Action("down") {
                    override fun run() {
                        Log.i(TAG, "HBM ok")
                    }
                })
                mThreadHelper.getBgHander().post {
                    mFingerprintManager.processCmd(BiometricCmd.CMD_DOWN, 0, 0 , byteArrayOf(), 0)
                    setHBM(true)
                    mThreadHelper.synFingerprint{
                        if(mState == ProcessState.DOWNING){
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

                mState = ProcessState.UPING
            }
            ProcessState.UI_READY -> {
                mState = ProcessState.UPING

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