package cn.nubia.systemui

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Process
import android.util.Log
import cn.nubia.systemui.NubiaSystemUIApplication.Companion.TAG
import cn.nubia.systemui.common.DumpHelper
import java.io.*
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

@Suppress("UNCHECKED_CAST")
class NubiaThreadHelper private constructor(): DumpHelper.Dump {

    private val mMainHandler:Handler = Handler(Looper.getMainLooper())
    private val DEBUG = false
    val mQueueList = LinkedList<Queue<Any>>()
    private val mFpFrontHandler:Handler by lazy {
        HandlerThread("FpFrontThread").let {
            it.start()
            it.looper
        }.let {
            Handler(it).apply {
                Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY+1)
            }
        }
    }

    private val mFpBgHandler:Handler by lazy {
        HandlerThread("FpBgThread").let {
            it.start()
            it.looper
        }.let {
            Handler(it).apply {
                Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT+1)
            }
        }
    }

    private val mSurfaceHandler:Handler by lazy {
        HandlerThread("SurfaceThread").let {
            it.start()
            it.looper
        }.let {
            Handler(it).apply {
                Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY-1)
            }
        }
    }

    init {
        handlerFpBg{
            registerDump()
        }
        if(DEBUG){
            mMainHandler.looper.setMessageLogging{
                Log.i(TAG, it)
            }
            mFpFrontHandler.looper.setMessageLogging{
                Log.i(TAG, it)
            }
            mSurfaceHandler.looper.setMessageLogging{
                Log.i(TAG, it)
            }
        }
    }

    fun getMainHander():Handler{
        return mMainHandler
    }

    fun getSurfaceHandler():Handler{
        return mSurfaceHandler
    }

    fun getFpFrontHander():Handler{
        return mFpFrontHandler
    }

    fun getFpBgHander():Handler{
        return mFpBgHandler
    }

    @Synchronized fun pollQueue(): Queue<Any> {
        return if(mQueueList.size > 0){
            mQueueList.remove()
        }else{
            LinkedBlockingQueue<Any>()
        }
    }

    @Synchronized fun peekQueue(queue: Queue<Any>){
        if(mQueueList.size<10){
            mQueueList.add(queue)
        }
    }

    fun <T> synFpBgInvoke(action: ()->T):T? = synInvoke(mFpBgHandler, action)

    fun <T> synFpFrontInvoke(action: ()->T):T? = synInvoke(mFpFrontHandler, action)

    fun <T> synMainInvoke(action: ()->T):T? = synInvoke(mMainHandler, action)

    fun <T> synInvoke(handler: Handler, action: ()->T):T?{
        return  if (Thread.currentThread() == handler.looper.thread){
            action.invoke()
        }else{
            val queue = pollQueue()
            try {
                handler.post{
                    queue.add(action.invoke())
                }
                queue.poll() as? T
            }finally {
                peekQueue(queue)
            }
        }
    }

    fun synFpBg(action: ()->Unit) = synInvoke(mFpBgHandler, action)

    fun synFpFront(action: ()->Unit) = synInvoke(mFpFrontHandler, action)

    fun synMain(action: ()->Unit) = synInvoke(mMainHandler, action)

    fun synInvoke(handler: Handler, action: ()->Unit){
        if (Thread.currentThread() == handler.looper.thread){
            action.invoke()
        }else{
            val queue = pollQueue()
            try {
                handler.post{
                    queue.add(action.invoke())
                }
                queue.poll()
            }finally {
                peekQueue(queue)
            }
        }
    }

    fun handlerFpBg(action: ()->Unit) = handlerInvoke(mFpBgHandler, action)

    fun handlerFpFront(action: ()->Unit) = handlerInvoke(mFpFrontHandler, action)

    fun handlerMain(action: ()->Unit) = handlerInvoke(mMainHandler, action)

    fun handlerInvoke(handler: Handler, action: ()->Unit){
        if (Thread.currentThread() == handler.looper.thread){
            action.invoke()
        }else{
            handler.post(action)
        }
    }

    companion object {
        private  var mHelp: NubiaThreadHelper? = null
            get(){
                if (field == null){
                    field = NubiaThreadHelper()
                }
                return field
            }

        public fun get(): NubiaThreadHelper {
            return mHelp!!
        }
    }


    override fun dump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?) {
        writer?.apply {
            mMainHandler.dump({
                write("${it}\n")
            }, "     ")
            mFpFrontHandler.dump({
                write("${it}\n")
            }, "     ")
            mSurfaceHandler.dump({
                write("${it}\n")
            }, "     ")
            mFpBgHandler.dump({
                write("${it}\n")
            }, "     ")
        }
    }
}