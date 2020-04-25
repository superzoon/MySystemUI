package cn.nubia.systemui

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Process
import android.util.Log
import android.util.PrintWriterPrinter
import cn.nubia.systemui.NubiaSystemUIApplication.Companion.TAG
import cn.nubia.systemui.common.Dump
import java.io.FileDescriptor
import java.io.PrintWriter
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

@Suppress("UNCHECKED_CAST")
class NubiaThreadHelper private constructor():Dump{

    private val mMainHandler:Handler = Handler(Looper.getMainLooper())
    val mQueueList = LinkedList<Queue<Any>>()

    private val mFingerprintHandler:Handler by lazy {
        HandlerThread("FpThread").let {
            it.start()
            it.looper
        }.let {
            Handler(it).apply {
                Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY+1)
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

    private val mBackgroundHandler:Handler by lazy {
        HandlerThread("BgThread").let {
            it.start()
            it.looper
        }.let {
            Handler(it).apply {
                Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT+1)
            }
        }
    }

    init {
        registerDump()
        mMainHandler.looper.setMessageLogging{
            Log.i(TAG, it)
        }
        mFingerprintHandler.looper.setMessageLogging{
            Log.i(TAG, it)
        }
        mSurfaceHandler.looper.setMessageLogging{
            Log.i(TAG, it)
        }
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
            action.invoke() as T
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
            mFingerprintHandler.dump({
                write("${it}\n")
            }, "     ")
            mSurfaceHandler.dump({
                write("${it}\n")
            }, "     ")
            mBackgroundHandler.dump({
                write("${it}\n")
            }, "     ")
        }
    }
}