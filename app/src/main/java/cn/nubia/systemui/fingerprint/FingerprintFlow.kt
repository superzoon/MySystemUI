package cn.nubia.systemui.fingerprint

import android.util.Log

abstract class  FingerprintFlow{
    val TAG by lazy { "Fp.${this.javaClass.simpleName}"}
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

    private var mState:FlowState = FlowState.NORMAL
    fun getState() = mState

    class ScreenOnFlow:FingerprintFlow() {
        override fun onDown() {
        }

        override fun onUiReady() {
        }

        override fun onUp() {
        }
    }

    class ScreenOffFlow:FingerprintFlow(){
        override fun onDown() {
        }

        override fun onUiReady() {
        }

        override fun onUp() {
        }
    }
    class ScreenAodFlow:FingerprintFlow(){
        override fun onDown() {
        }

        override fun onUiReady() {
        }

        override fun onUp() {
        }
    }
}