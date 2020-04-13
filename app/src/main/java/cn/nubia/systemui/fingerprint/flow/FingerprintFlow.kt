package cn.nubia.systemui.fingerprint.flow

import android.util.Log
import cn.nubia.systemui.NubiaSystemUIApplication
import cn.nubia.systemui.common.Dump
import cn.nubia.systemui.fingerprint.FingerprintController

abstract class  FingerprintFlow(val mController:FingerprintController):Dump{
    val TAG by lazy { "${NubiaSystemUIApplication.TAG}.${this.javaClass.simpleName}"}
    enum class FlowState{
        NORMAL, DOWN, UI_READY, UP
    }
    fun callDown(){
        when(getState()){
            FlowState.NORMAL, FlowState.UP -> {onDown()}
            else -> {Log.i(TAG, "ERROR down, mState=${getState()}")}
        }
    }
    abstract fun onDown()

    fun callUiReady(){
        when(getState()){
            FlowState.DOWN -> {onUiReady()}
            else -> {Log.i(TAG, "ERROR uiready, mState=${getState()}")}
        }
    }
    abstract fun onUiReady()
    fun callUp(){
        when(getState()){
            FlowState.UI_READY -> {onUp()}
            else -> {Log.i(TAG, "ERROR up, mState=${getState()}")}
        }
    }
    abstract fun onUp()

    private var mState: FlowState = FlowState.NORMAL
    fun getState() = mState

    class ScreenOnFlow(mController:FingerprintController): FingerprintFlow(mController) {
        override fun onDown() {
        }

        override fun onUiReady() {
        }

        override fun onUp() {
        }
    }

    class ScreenOffFlow(mController:FingerprintController): FingerprintFlow(mController) {
        override fun onDown() {
        }

        override fun onUiReady() {
        }

        override fun onUp() {
        }
    }
    class ScreenAodFlow(mController:FingerprintController): FingerprintFlow(mController) {
        override fun onDown() {
        }

        override fun onUiReady() {
        }

        override fun onUp() {
        }
    }
}