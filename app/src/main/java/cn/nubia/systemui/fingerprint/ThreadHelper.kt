package cn.nubia.systemui.fingerprint

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Process
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

class ThreadHelper private constructor(){

    interface Action<T>{
        fun action():T;
    }

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

    fun <T> synBackground(action: ThreadHelper.Action<T>):T{
        return _syn_(action, mBackgroundHandler)
    }

    fun <T> synFingerprint(action: ThreadHelper.Action<T>):T{
        return _syn_(action, mFingerprintHandler)
    }

    fun <T> synMain(action: ThreadHelper.Action<T>):T{
        return _syn_(action, mMainHandler)
    }

    @SuppressWarnings("unchecked")
    private fun <T> _syn_(action: ThreadHelper.Action<T>, handler: Handler):T{
        return  if (Thread.currentThread() == handler.looper.thread){
            action.action()
        }else{
            val queue = pollQueue()
            try {
                handler.post{
                    queue.add(action.action())
                }
                queue.poll()!! as T
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