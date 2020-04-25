package cn.nubia.systemui.fingerprint.flow

import android.util.Log
import cn.nubia.systemui.NubiaSystemUIApplication
import cn.nubia.systemui.common.Dump
import cn.nubia.systemui.fingerprint.FingerprintController
import cn.nubia.systemui.fingerprint.ThreadHelper
import cn.nubia.systemui.fingerprint.setHBM

abstract class  FingerprintFlow(val mController:FingerprintController):Dump{
    val TAG by lazy { "${NubiaSystemUIApplication.TAG}.${this.javaClass.simpleName}"}
    enum class FlowState{
        NORMAL, DOWN, UI_READY, UP
    }

    private var mState: FlowState = FlowState.NORMAL
        get() = field
        set(value) {
            if(field!=value){
                Log.i(TAG, "flow state change")
                field=value
            }
        }

    fun onTouchDown() = when(mState){
        FlowState.NORMAL -> {
            mState = FlowState.DOWN
            mController.hbmAction(object :FlowAction(""){
                override fun run() {
                    super.run()
                }
            })
            ThreadHelper.get().apply {
                getBgHander().post {
                    setHBM(true)
                    synFingerprint{
                        mController.onHbmEnable(true)
                    }
                }
            }
        }
        FlowState.DOWN -> {

        }
        FlowState.UI_READY -> {
        }
        FlowState.UP -> {

        }
    }

    fun onUiReady(){
        when(mState){
            FlowState.NORMAL -> {

            }
            FlowState.DOWN -> {
                mState = FlowState.UI_READY
            }
            FlowState.UI_READY -> {
            }
            FlowState.UP -> {

            }
        }
    }
    fun onTouchUp(){
        when(mState){
            FlowState.NORMAL -> {

            }
            FlowState.DOWN -> {

            }
            FlowState.UI_READY -> {
                mState = FlowState.UP
            }
            FlowState.UP -> {

            }
        }
    }

    fun onIconShow() {}
    fun onIconHide() {}
    fun onStartAuth(owner: String?) { }
    fun onDoneAuth() { }
    fun onAcquired(info: Int) { }
    fun onAuthError() { }
    fun onFailAuth() { }
    fun onStopAuth() { }

    class ScreenOnFlow(mController:FingerprintController): FingerprintFlow(mController) {
    }

    class ScreenOffFlow(mController:FingerprintController): FingerprintFlow(mController) {
    }
    class ScreenAodFlow(mController:FingerprintController): FingerprintFlow(mController) {
    }
}