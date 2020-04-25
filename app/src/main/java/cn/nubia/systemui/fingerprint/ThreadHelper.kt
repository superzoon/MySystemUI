package cn.nubia.systemui.fingerprint

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Process
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

@Suppress("UNCHECKED_CAST")
class ThreadHelper private constructor(){

    val mQueueList = LinkedList<Queue<Any>>()

    private val mMainHandler:Handler by lazy {

        Handler(Looper.getMainLooper())
    }

    private val mFingerprintHandler:Handler by lazy {
        var t = HandlerThread("FpThread")
        t.start()
        val handler = Handler(t.looper)
        handler.post{
            Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY+1)
        }
        handler
    }

    private val mSurfaceHandler:Handler by lazy {
        var t = HandlerThread("SurfaceThread")
        t.start()
        val handler = Handler(t.looper)
        handler.post{
            Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY-1)
        }
        handler
    }

    private val mBackgroundHandler:Handler by lazy {
        var t = HandlerThread("BgThread")
        t.start()
        val handler = Handler(t.looper)
        handler.post{
            Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT+1)
        }
        handler
    }

    fun getMainHander():Handler{
        return mMainHandler
    }

    fun getSurfaceHandler():Handler{
        return mSurfaceHandler
    }

    fun getFingerHander():Handler{
        return mFingerprintHandler
    }

    fun getBgHander():Handler{
        return mBackgroundHandler
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

    fun <T> synBackgroundInvoke(action: ()->T?):T?{
        return _syn_invoke_(action, mBackgroundHandler)
    }

    fun <T> synFingerprintInvoke(action: ()->T?):T?{
        return _syn_invoke_(action, mFingerprintHandler)
    }

    fun <T> synMainInvoke(action: ()->T?):T?{
        return _syn_invoke_(action, mMainHandler)
    }


    private fun <T> _syn_invoke_(action: ()->T?, handler: Handler):T?{
        return  if (Thread.currentThread() == handler.looper.thread){
            action.invoke() as? T
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

    fun synBackground(action: ()->Unit){
        _syn_(action, mBackgroundHandler)
    }

    fun synFingerprint(action: ()->Unit){
        _syn_(action, mFingerprintHandler)
    }

    fun synMain(action: ()->Unit){
        _syn_(action, mMainHandler)
    }

    private fun _syn_(action: ()->Unit, handler: Handler):Any?{
        return  if (Thread.currentThread() == handler.looper.thread){
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

    companion object {
        private  var mHelp:ThreadHelper? = null
            get(){
                if (field == null){
                    field = ThreadHelper()
                }
                return field
            }

        public fun get():ThreadHelper{
            return mHelp!!
        }
    }
}