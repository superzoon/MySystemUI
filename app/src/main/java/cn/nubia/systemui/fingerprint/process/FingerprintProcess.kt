package cn.nubia.systemui.fingerprint.process

import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.util.Log
import cn.nubia.systemui.NubiaSystemUIApplication
import cn.nubia.systemui.common.Dump
import cn.nubia.systemui.fingerprint.FingerprintController
import cn.nubia.systemui.NubiaThreadHelper
import cn.nubia.systemui.fingerprint.setHBM

abstract class  FingerprintProcess(val mContext:Context, val mController:FingerprintController):Dump{
    val TAG by lazy { "${NubiaSystemUIApplication.TAG}.${this.javaClass.simpleName}"}

    val mFingerprintManager :FingerprintManager = mContext.getSystemService(FingerprintManager::class.java)



    enum class ProcessState{
        NORMAL, DOWN, UI_READY, UP
    }

    private var mState: ProcessState = ProcessState.NORMAL
        get() = field
        set(value) {
            if(field!=value){
                Log.i(TAG, "flow state change")
                field=value
            }
        }

    fun onTouchDown() = when(mState){
        ProcessState.NORMAL -> {
            mState = ProcessState.DOWN
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
        ProcessState.DOWN -> {

        }
        ProcessState.UI_READY -> {
        }
        ProcessState.UP -> {

        }
    }

    fun onUiReady(){
        when(mState){
            ProcessState.NORMAL -> {

            }
            ProcessState.DOWN -> {
                mState = ProcessState.UI_READY
            }
            ProcessState.UI_READY -> {
            }
            ProcessState.UP -> {

            }
        }
    }
    fun onTouchUp(){
        when(mState){
            ProcessState.NORMAL -> {

            }
            ProcessState.DOWN -> {

            }
            ProcessState.UI_READY -> {
                mState = ProcessState.UP
            }
            ProcessState.UP -> {

            }
        }
    }

    open fun onIconShow() {}
    open fun onIconHide() {}
    open fun onStartAuth(owner: String?) { }
    open fun onDoneAuth() { }
    open fun onAcquired(info: Int) { }
    open fun onAuthError() { }
    open fun onFailAuth() { }
    open fun onStopAuth() { }

}