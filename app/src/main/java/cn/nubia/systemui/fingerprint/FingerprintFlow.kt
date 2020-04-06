package cn.nubia.systemui.fingerprint

abstract class  FingerprintFlow{
    abstract fun onDown()
    abstract fun onUiReady()
    abstract fun onUp()

    enum class State{
        NORMAL, DOWN, UI_READY, UP
    }

    private var mState:State = State.NORMAL
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