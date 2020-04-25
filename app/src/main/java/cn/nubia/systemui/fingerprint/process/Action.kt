package cn.nubia.systemui.fingerprint.process

abstract class Action(val name:String, val action:Runnable?=null):Runnable {
    override fun run() {
        action?.run()
    }

    fun invoke():Boolean{
        action?.run()
        return true
    }
}