package cn.nubia.systemui.fingerprint.action

import android.view.Display
import cn.nubia.systemui.fingerprint.annotation.ActionKeyInt
import cn.nubia.systemui.common.Controller
import java.lang.AssertionError

class ActionEvent(val controller: Controller) {

    class Action(val name:String, val action:()->Unit):Runnable{
        override fun run() {
            action()
        }

        fun invoke():Boolean{
            run()
            return true
        }

        override fun toString(): String {
            return "${name} -> ${action}"
        }
    }

    class ActionKey {
        companion object {
            val KEY_SCREEN_OFF = Display.STATE_OFF
            val KEY_SCREEN_ON = Display.STATE_ON
            val KEY_SCREEN_DOZE = Display.STATE_DOZE
            val KEY_SCREEN_HBM = 1.shl(4)
            val KEY_ON_FINGER_DOWN = 1.shl(7)
            val KEY_ON_FINGER_UI_READY = 1.shl(8)
            val KEY_ON_FINGER_UP = 1.shl(9)

            operator fun contains(key: Int): Boolean {
                return (key == KEY_SCREEN_OFF) ||
                        (key <= KEY_SCREEN_ON) ||
                        (key <= KEY_SCREEN_DOZE) ||
                        (key <= KEY_SCREEN_HBM) ||
                        (key <= KEY_ON_FINGER_DOWN)||
                        (key <= KEY_ON_FINGER_UI_READY)||
                        (key <= KEY_ON_FINGER_UP)
            }
        }
    }

    private val SCREEN_OFF_ACTIONS = mutableListOf<Action>()
    private val SCREEN_DOZE_ACTIONS = mutableListOf<Action>()
    private val SCREEN_ON_ACTIONS = mutableListOf<Action>()
    private val SCREEN_HBM_ACTIONS = mutableListOf<Action>()
    private val FINGER_DOWN_ACTIONS = mutableListOf<Action>()
    private val FINGER_UI_READY_ACTIONS = mutableListOf<Action>()
    private val FINGER_UP_ACTIONS = mutableListOf<Action>()


    private val mProcessAction: Map<Int, MutableList<Action>> = mapOf(
            ActionKey.KEY_SCREEN_OFF to SCREEN_OFF_ACTIONS,
            ActionKey.KEY_SCREEN_ON to SCREEN_ON_ACTIONS,
            ActionKey.KEY_SCREEN_DOZE to SCREEN_DOZE_ACTIONS,
            ActionKey.KEY_SCREEN_HBM to SCREEN_HBM_ACTIONS,
            ActionKey.KEY_ON_FINGER_DOWN to FINGER_DOWN_ACTIONS,
            ActionKey.KEY_ON_FINGER_UI_READY to FINGER_UI_READY_ACTIONS,
            ActionKey.KEY_ON_FINGER_UP to FINGER_UP_ACTIONS
    )

    operator fun get(@ActionKeyInt key: Int): MutableList<Action> {
        return when (key) {
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

    fun addFingerDown(flowAction: Action) {
        controller.checkThread()
        addAction(ActionKey.KEY_ON_FINGER_DOWN, flowAction)
    }

    fun addFingerUIReady(flowAction: Action) {
        controller.checkThread()
        addAction(ActionKey.KEY_ON_FINGER_UI_READY, flowAction)
    }

    fun addFingerUp(flowAction: Action) {
        controller.checkThread()
        addAction(ActionKey.KEY_ON_FINGER_UP, flowAction)
    }

    fun addAction(@ActionKeyInt key: Int, flowAction: Action) {
        controller.checkThread()
        if (flowAction !in this[key]) {
            this[key].add(flowAction)
        }
    }

    fun removeDozeAction(flowAction: Action) {
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

    fun removeFingerDown(flowAction: Action) {
        controller.checkThread()
        removeAction(ActionKey.KEY_ON_FINGER_DOWN, flowAction)
    }

    fun removeFingerUIReady(flowAction: Action) {
        controller.checkThread()
        removeAction(ActionKey.KEY_ON_FINGER_UI_READY, flowAction)
    }

    fun removeFingerUp(flowAction: Action) {
        controller.checkThread()
        removeAction(ActionKey.KEY_ON_FINGER_UP, flowAction)
    }

    fun removeAction(@ActionKeyInt key: Int, flowAction: Action) {
        controller.checkThread()
        if (flowAction in this[key]) {
            this[key].remove(flowAction)
        }
    }

    fun clearAction(@ActionKeyInt key: Int) {
        controller.checkThread()
        this[key].clear()
    }

    fun clearAllAction() {
        controller.checkThread()
        mProcessAction.forEach { t, u ->
            u.clear()
        }
    }

    fun invoke(@ActionKeyInt key: Int) {
        if (key in ActionKey) {
            this[key].forEach() {
                it.invoke()
            }
            this[key].clear()
        } else {
            throw IllegalAccessError("Illegal key=${key}")
        }
    }
}

