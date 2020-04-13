package cn.nubia.systemui.fingerprint.flow

abstract class FlowAction(val name:String, val action:Runnable?=null):Runnable {
    override fun run() {
        action?.run()
    }
}