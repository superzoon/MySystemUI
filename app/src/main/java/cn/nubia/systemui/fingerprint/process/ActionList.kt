package cn.nubia.systemui.fingerprint.process

import android.view.Display
import java.lang.AssertionError


class ActionList {
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
}
