package cn.nubia.systemui.common

import android.util.Log
import cn.nubia.systemui.NubiaSystemUIApplication
import cn.nubia.systemui.fingerprint.ThreadHelper
import java.io.FileDescriptor
import java.io.PrintWriter
import java.lang.ref.Reference
import java.lang.ref.WeakReference

interface Dump {
    companion object {
        private val TAG = "${NubiaSystemUIApplication.TAG}.Dump"
        private val mList = mutableListOf<Reference<Dump>>()
        private val mHandler = ThreadHelper.get().getMainHander()

        private fun registerDump(dump:Dump?){
            dump.apply {
                mHandler.post{
                    if(mList.find { it.get()== dump } == null){
                        mList.add(WeakReference(dump))
                    }
                    unregisterDump(null)
                    Log.i(TAG, "addCallback size=${mList.size}")
                }
            }
        }

        private fun unregisterDump(dump:Dump?){
            mHandler.post{
                mList.removeAll { it.get() == dump }
            }
        }

        fun dipatchDump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?){
            mList.forEach { it.get()?.dump(fd, writer, args) }
        }
    }
    fun registerDump(){
        Dump.registerDump(this)
    }

    fun unregisterDump(){
        Dump.unregisterDump(this)
    }
    fun dump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?)
}