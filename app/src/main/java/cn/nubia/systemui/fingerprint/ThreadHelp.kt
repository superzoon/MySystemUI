package cn.nubia.systemui.fingerprint

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

class ThreadHelp private constructor(){

    private val mMainHandler:Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    private val mFingerprintHandler:Handler by lazy {
        var t = HandlerThread("")
        t.start()
        Handler(t.looper)
    }

    interface Action<T>{
        fun action():T;
    }

    fun getFingerHander():Handler{
        return mFingerprintHandler
    }

    val mQueueList = LinkedList<Queue<Any>>()

    @Synchronized fun pollQueue(): Queue<Any> {
        return if(mQueueList.size > 0){
            mQueueList.remove()
        }else{
            LinkedBlockingQueue<Any>()
        }
    }

    @Synchronized fun peekQueue(queue: Queue<Any>){
        if(mQueueList.size<5){
            mQueueList.add(queue)
        }
    }

    fun <T> synFingerprint(action: ThreadHelp.Action<T>):T{
        return syn(action, mFingerprintHandler)
    }

    fun <T> synMain(action: ThreadHelp.Action<T>):T{
        return syn(action, mMainHandler)
    }

    @SuppressWarnings("unchecked")
    fun <T> syn(action: ThreadHelp.Action<T>, handler: Handler):T{
        val queue = pollQueue()
        try {
            handler.post{
                queue.add(action.action())
            }
            var obj = queue.poll()
            return obj!! as T

        }finally {
            peekQueue(queue)
        }
    }
    companion object {
        private  var mHelp:ThreadHelp? = null
            get(){
                if (field == null){
                    field = ThreadHelp()
                }
                return field
            }

        public fun get():ThreadHelp{
            return mHelp!!
        }
    }
}