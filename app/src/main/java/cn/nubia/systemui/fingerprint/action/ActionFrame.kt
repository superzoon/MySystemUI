package cn.nubia.systemui.fingerprint.action

import android.view.Choreographer
import cn.nubia.systemui.NubiaThreadHelper
import cn.nubia.systemui.common.Controller

class ActionFrame(val controller: Controller){

    class Action(val mNFrame:Int=1, val mName:String="frame", val mAction:()->Unit):Runnable{
        private var mCurrentNFrame = mNFrame
        override fun run() {
            mAction()
        }

        fun invoke():Boolean{
            mCurrentNFrame--
            if(mCurrentNFrame<=0){
                run()
                return true
            }else{
                return false
            }
        }

        override fun toString(): String {
            return "${mName} -> ${mNFrame} ->${mCurrentNFrame} -> ${mAction}"
        }
    }

    private val SCREEN_FRAME_ACTIONS = mutableListOf<Action>()

    private val mChoreographer by lazy {
        NubiaThreadHelper.get().synMainInvoke{
            Choreographer.getInstance()
        }!!
    }

    private fun doFrameAnimation(frameTimeNanos:Long){
        NubiaThreadHelper.get().handlerInvoke(controller.getHandler()){
            SCREEN_FRAME_ACTIONS.filter{
                !it.invoke()
            }
            if(SCREEN_FRAME_ACTIONS.size>0){
                mChoreographer.postFrameCallback{::doFrameAnimation}
            }
        }
    }

    fun addFrameAction(action: Action) {
        controller.checkThread()
        if(action.mNFrame<=0){
            NubiaThreadHelper.get().handlerInvoke(controller.getHandler()){
                action.invoke()
            }
        }else{
            val size = SCREEN_FRAME_ACTIONS.size
            SCREEN_FRAME_ACTIONS.add(action)
            if (size == 0) {
                mChoreographer.postFrameCallback{::doFrameAnimation}
            }
        }
    }

    fun removeFrameAction(action: Action) {
        controller.checkThread()
        if(action in SCREEN_FRAME_ACTIONS){
            SCREEN_FRAME_ACTIONS.remove(action)
        }
    }
}