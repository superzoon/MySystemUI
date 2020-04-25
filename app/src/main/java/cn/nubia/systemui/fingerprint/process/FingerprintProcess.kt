package cn.nubia.systemui.fingerprint.process

import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.util.Log
import cn.nubia.systemui.NubiaSystemUIApplication
import cn.nubia.systemui.common.Dump
import cn.nubia.systemui.fingerprint.FingerprintController
import cn.nubia.systemui.NubiaThreadHelper
import cn.nubia.systemui.common.BiometricCmd
import cn.nubia.systemui.common.ErrorInfo
import cn.nubia.systemui.common.processCmd
import cn.nubia.systemui.fingerprint.setHBM
import java.io.FileDescriptor
import java.io.PrintWriter

abstract class  FingerprintProcess(val mContext:Context, val mController:FingerprintController){
    val TAG by lazy { "${NubiaSystemUIApplication.TAG}.${this.javaClass.simpleName}"}

    val mFingerprintManager :FingerprintManager = mContext.getSystemService(FingerprintManager::class.java)

    enum class ProcessState{
        NORMAL, DOWN, UI_READY, UP
    }

    private var mState: ProcessState = ProcessState.NORMAL
        get() = field
        set(value) {
            if(field!=value){
                Log.i(TAG, "process state change")
                field=value
            }
        }

    fun onTouchDown() = when(mState){
        ProcessState.NORMAL ,ProcessState.UP  -> {
            mState = ProcessState.DOWN
            mFingerprintManager.processCmd(BiometricCmd.CMD_DOWN, 0, 0 , byteArrayOf(), 0)
            mController.addHbmAction(object :Action(""){
                override fun run() {
                    super.run()
                }
            })
            NubiaThreadHelper.get().apply {
                getBgHander().post {
                    setHBM(true)
                    synFingerprint{
                        mController.onHbmEnable(true)
                    }
                }
            }
        }
        else -> {
            Log.w(TAG, "onTouchDown, but current state = ${mState}")
        }
    }

    fun onUiReady(){
        when(mState){
            ProcessState.DOWN -> {
                mState = ProcessState.UI_READY
                mFingerprintManager.processCmd(BiometricCmd.CMD_UI_READY, 0, 0 , byteArrayOf(), 0)
            }
            else -> {
                Log.w(TAG, "onUiReady, but current state = ${mState}")
            }
        }
    }

    fun onTouchUp(){
        when(mState){
            ProcessState.UI_READY -> {
                mState = ProcessState.UP
                mFingerprintManager.processCmd(BiometricCmd.CMD_UP, 0, 0 , byteArrayOf(), 0)

                NubiaThreadHelper.get().apply {
                    getBgHander().post {
                        setHBM(false)
                        synFingerprint{
                            mController.onHbmEnable(false)
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
            in ErrorInfo -> {
                if (ErrorInfo isVibrateError info){

                }
            }
        }
    }

    open fun onIconShow() {}
    open fun onIconHide() {}
    open fun onStartAuth(owner: String?) { }
    open fun onDoneAuth() { }
    open fun onAuthError() { }
    open fun onFailAuth() { }
    open fun onStopAuth() { }
    abstract fun dump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?)
}