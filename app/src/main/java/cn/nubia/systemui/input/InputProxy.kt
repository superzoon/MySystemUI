package cn.nubia.systemui.input

import android.content.Context
import android.hardware.input.InputManager
import android.util.Log
import android.view.*
import cn.nubia.systemui.NubiaSystemUIApplication
import cn.nubia.systemui.NubiaThreadHelper
import java.lang.Exception
import java.lang.ref.Reference
import java.lang.ref.WeakReference


class InputProxy private constructor(val mContext:Context) {
    interface EventListener{
        fun onTouchEvent(event:MotionEvent)
        fun onKeyEvent(event:KeyEvent)
    }

    companion object {
        val TAG = "${NubiaSystemUIApplication.TAG}.Input"
        private var proxy:InputProxy?=null
        fun get(mContext:Context) = if(proxy!=null){
            proxy!!
        }else{
            synchronized(this){
                if(proxy==null){
                    proxy = InputProxy(mContext)
                }
            }
            proxy!!
        }
    }

    private val mHandler = NubiaThreadHelper.get().getBgHander()
    private val mInputManager = mContext.getSystemService(InputManager::class.java)
    private val mList = mutableListOf<Reference<EventListener>>()
    private var receiver:InputEventReceiver?
    init {
        receiver = getInputChannel(getInputMonitor(""))?.let {
            object : InputEventReceiver(it as InputChannel, mHandler.looper){
                override fun onInputEvent(event: InputEvent?) {
                    Log.i(TAG, "on onInputEvent ${event}")
                    var handled = false
                    try {
                        if (event is MotionEvent && event.getSource() and InputDevice.SOURCE_CLASS_POINTER != 0) {
                            dispatchPointer(MotionEvent.obtainNoHistory(event))
                            handled = true
                        }else if(event is KeyEvent && event.getSource() and InputDevice.SOURCE_CLASS_POINTER != 0){
                            dispatchKey(KeyEvent(event as KeyEvent))
                            handled = true
                        }
                    } finally {
                        finishInputEvent(event, handled)
                    }
                }
            }
        }
    }

    private fun dispatchKey(event: KeyEvent) {
        mHandler.post{
            mList.forEach{
                it.get()?.onKeyEvent(event)
            }
        }
    }

    private fun dispatchPointer(event: MotionEvent) {
        mHandler.post{
            mList.forEach{
                it.get()?.onTouchEvent(event)
            }
        }
    }


    fun registerEventListener(callback: EventListener?){
        callback.apply {
            mHandler.post{
                if(mList.find { it.get()== this } == null){
                    mList.add(WeakReference(this))
                }
                unregisterEventListener(null)
                Log.i(TAG, "addEventListener size=${mList.size}")
            }
        }
    }

    fun unregisterEventListener(callback: EventListener?){
        mHandler.post{
            mList.removeAll { it.get() == callback }
        }
    }

    private fun getInputMonitor(name:String, displayId:Int=Display.DEFAULT_DISPLAY):Any?{
        try {
            return InputManager::class.java.getDeclaredMethod("monitorGestureInput", String::class.java, Int.javaClass)
                    .invoke(mInputManager, name, displayId)
        }catch (e:Exception){
            e.printStackTrace()
            return null;
        }
    }

    private fun getInputChannel(inputMonitor:Any?):Any?{
        return inputMonitor?.let {
            it.javaClass.getDeclaredMethod("getInputChannel").invoke(it)
        }
    }
}