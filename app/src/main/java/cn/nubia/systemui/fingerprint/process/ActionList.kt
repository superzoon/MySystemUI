package cn.nubia.systemui.fingerprint.process

import android.support.annotation.IntDef
import android.view.Choreographer
import android.view.Display
import cn.nubia.systemui.NubiaThreadHelper
import cn.nubia.systemui.common.Controller
import java.lang.AssertionError

class ActionList(val controller: Controller) {

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

    @IntDef(value = longArrayOf(ActionKey.KEY_SCREEN_OFF.toLong(),
            ActionKey.KEY_SCREEN_ON.toLong(),
            ActionKey.KEY_SCREEN_DOZE.toLong(),
            ActionKey.KEY_SCREEN_HBM.toLong(),
            ActionKey.KEY_SCREEN_FRAME.toLong(),
            ActionKey.KEY_SCREEN_2FRAME.toLong()))
    @Retention(AnnotationRetention.SOURCE)
    annotation class KeyInt


    class ActionKey {
        companion object {
            val KEY_SCREEN_OFF = Display.STATE_OFF
            val KEY_SCREEN_ON = Display.STATE_ON
            val KEY_SCREEN_DOZE = Display.STATE_DOZE
            val KEY_SCREEN_HBM = 1.shl(4)
            val KEY_SCREEN_FRAME = 1.shl(5)
            val KEY_SCREEN_2FRAME = 1.shl(6)

            operator fun contains(key: Int): Boolean {
                return (key == KEY_SCREEN_OFF) ||
                        (key <= KEY_SCREEN_ON) ||
                        (key <= KEY_SCREEN_DOZE) ||
                        (key <= KEY_SCREEN_HBM) ||
                        (key <= KEY_SCREEN_FRAME)||
                        (key <= KEY_SCREEN_2FRAME)
            }
        }
    }

    private val SCREEN_OFF_ACTIONS = mutableListOf<Action>()
    private val SCREEN_DOZE_ACTIONS = mutableListOf<Action>()
    private val SCREEN_ON_ACTIONS = mutableListOf<Action>()
    private val SCREEN_HBM_ACTIONS = mutableListOf<Action>()
    private val SCREEN_FRAME_ACTIONS = mutableListOf<Action>()
    private val SCREEN_2FRAME_ACTIONS = mutableListOf<Action>()

    private val mChoreographer by lazy {
        NubiaThreadHelper.get().synMainInvoke{
            Choreographer.getInstance()
        }!!
    }

    private val mProcessAction: Map<Int, MutableList<Action>> = mapOf(
            ActionKey.KEY_SCREEN_OFF to SCREEN_OFF_ACTIONS,
            ActionKey.KEY_SCREEN_ON to SCREEN_ON_ACTIONS,
            ActionKey.KEY_SCREEN_DOZE to SCREEN_DOZE_ACTIONS,
            ActionKey.KEY_SCREEN_HBM to SCREEN_HBM_ACTIONS,
            ActionKey.KEY_SCREEN_FRAME to SCREEN_FRAME_ACTIONS,
            ActionKey.KEY_SCREEN_2FRAME to SCREEN_2FRAME_ACTIONS
    )

    operator fun get(@KeyInt key: Int): MutableList<Action> {
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

    fun addFrameAction(flowAction: Action) {
        controller.checkThread()
        if(this[ActionKey.KEY_SCREEN_FRAME].size==0){
            NubiaThreadHelper.get().apply {
                synMainInvoke {
                    mChoreographer.postFrameCallback {
                        handlerInvoke(controller.getHandler()){
                            invoke(ActionKey.KEY_SCREEN_FRAME)
                            this@ActionList[ActionKey.KEY_SCREEN_2FRAME].removeAll{
                                addFrameAction(it)
                                true
                            }
                        }
                    }
                }
            }
        }
        addAction(ActionKey.KEY_SCREEN_FRAME, flowAction)
    }

    fun add2FrameAction(flowAction: Action) {
        controller.checkThread()
        if(this[ActionKey.KEY_SCREEN_FRAME].size==0){
            NubiaThreadHelper.get().apply {
                synMainInvoke {
                    mChoreographer.postFrameCallback {
                        handlerInvoke(controller.getHandler()){
                            this@ActionList[ActionKey.KEY_SCREEN_2FRAME].removeAll{
                                addFrameAction(it)
                                true
                            }
                        }
                    }
                }
            }
        }
        addAction(ActionKey.KEY_SCREEN_2FRAME, flowAction)
    }

    fun addAction(@KeyInt key: Int, flowAction: Action) {
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

    fun removeFrameAction(flowAction: Action) {
        controller.checkThread()
        removeAction(ActionKey.KEY_SCREEN_FRAME, flowAction)
    }

    fun remove2FrameAction(flowAction: Action) {
        controller.checkThread()
        removeAction(ActionKey.KEY_SCREEN_2FRAME, flowAction)
    }

    fun removeAction(@KeyInt key: Int, flowAction: Action) {
        controller.checkThread()
        if (flowAction in this[key]) {
            this[key].remove(flowAction)
        }
    }

    fun clearAction(@KeyInt key: Int) {
        controller.checkThread()
        this[key].clear()
    }

    fun invoke(@KeyInt key: Int) {
        if (key in ActionKey) {
            this[key].removeAll {
                it.invoke()
                true
            }
        } else {
            throw IllegalAccessError("Illegal key=${key}")
        }
    }
}

