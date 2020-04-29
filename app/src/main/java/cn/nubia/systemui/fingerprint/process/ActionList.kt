package cn.nubia.systemui.fingerprint.process

import android.view.Display
import cn.nubia.systemui.common.Controller
import java.lang.AssertionError


class ActionList (val controller: Controller){
    class ActionKey{
        companion object {
            val KEY_SCREEN_OFF = Display.STATE_OFF
            val KEY_SCREEN_ON = Display.STATE_ON
            val KEY_SCREEN_DOZE = Display.STATE_DOZE
            val KEY_SCREEN_HBM = 1.shl(4)

            operator fun contains(key:Int):Boolean{
                return (key == KEY_SCREEN_OFF) or
                        (key <= KEY_SCREEN_ON)or
                        (key <= KEY_SCREEN_DOZE)or
                        (key <= KEY_SCREEN_HBM)
            }

        }
    }

    private val SCREEN_OFF_ACTIONS = mutableListOf<Action>()
    private val SCREEN_DOZE_ACTIONS = mutableListOf<Action>()
    private val SCREEN_ON_ACTIONS = mutableListOf<Action>()
    private val SCREEN_HBM_ACTIONS = mutableListOf<Action>()

    private val mProcessAction: Map<Int, MutableList<Action>> = mapOf(
            ActionKey.KEY_SCREEN_OFF to SCREEN_OFF_ACTIONS,
            ActionKey.KEY_SCREEN_ON to SCREEN_ON_ACTIONS,
            ActionKey.KEY_SCREEN_DOZE to SCREEN_DOZE_ACTIONS,
            ActionKey.KEY_SCREEN_HBM to SCREEN_HBM_ACTIONS
    )

    operator fun get(key: Int): MutableList<Action> {
        return when(key){
            in ActionKey -> mProcessAction[key]!!
            else -> throw AssertionError("no key=${key} in list")
        }
    }

    fun addDozeAction(flowAction: Action) {
        controller.checkThread()
        addAction(ActionKey.KEY_SCREEN_DOZE, flowAction)
    }

    fun addScreenOffAction(flowAction: Action) {
        controller.checkThread()
        addAction(ActionKey.KEY_SCREEN_OFF, flowAction)
    }

    fun addScreenOnAction(flowAction: Action) {
        controller.checkThread()
        addAction(ActionKey.KEY_SCREEN_ON, flowAction)
    }

    fun addHbmAction(flowAction: Action) {
        controller.checkThread()
        addAction(ActionKey.KEY_SCREEN_HBM, flowAction)
    }

    fun addAction(key:Int, flowAction: Action) {
        controller.checkThread()
        if(flowAction !in this[key]){
            this[key].add(flowAction)
        }
    }

    fun removeDozeAction(flowAction: Action){
        controller.checkThread()
        removeAction(ActionKey.KEY_SCREEN_DOZE, flowAction)
    }

    fun removeScreenOffAction(flowAction: Action) {
        controller.checkThread()
        removeAction(ActionKey.KEY_SCREEN_OFF, flowAction)
    }

    fun removeScreenOnAction(flowAction: Action) {
        controller.checkThread()
        removeAction(ActionKey.KEY_SCREEN_ON, flowAction)
    }

    fun removeHbmAction(flowAction: Action) {
        controller.checkThread()
        removeAction(ActionKey.KEY_SCREEN_HBM, flowAction)
    }

    fun removeAction(key:Int, flowAction: Action) {
        controller.checkThread()
        if(flowAction in this[key]){
            this[key].remove(flowAction)
        }
    }

    fun clearAction(key:Int) {
        controller.checkThread()
        this[key].clear()
    }

    fun invoke(key:Int){
        this[ActionKey.KEY_SCREEN_HBM].removeAll{
            it.invoke()
        }
    }
}
