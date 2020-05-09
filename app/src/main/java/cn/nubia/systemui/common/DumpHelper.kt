package cn.nubia.systemui.common

import android.util.Log
import cn.nubia.systemui.NubiaSystemUIApplication
import cn.nubia.systemui.NubiaThreadHelper
import java.io.FileDescriptor
import java.io.PrintWriter
import java.lang.ref.Reference
import java.lang.ref.WeakReference

class DumpHelper {
    companion object {
        private val TAG = "${NubiaSystemUIApplication.TAG}.Dump"
        private val mList = mutableListOf<Reference<Dump>>()
        private val mHandler = NubiaThreadHelper.get().getMainHander()

        private fun registerDump(dump:Dump?){
            dump?.apply {
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
    interface Dump{
        fun dump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?){}
        fun registerDump(){
            DumpHelper.registerDump(this)
        }

        fun unregisterDump(){
            DumpHelper.unregisterDump(this)
        }
    }
}