package cn.nubia.systemui.fingerprint

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import cn.nubia.systemui.NubiaSystemUIApplication
import cn.nubia.systemui.common.DumpHelper
import java.io.FileDescriptor
import java.io.PrintWriter
import java.lang.ref.Reference
import java.lang.ref.WeakReference

class NubiaBiometricMonitor : DumpHelper.Dump {

    private val mHandler = Handler(Looper.getMainLooper());
    interface UpdateMonitorCallback{
        fun onStartAuth(owner:String?)
        fun onDoneAuth()
        fun onStopAuth()
        fun onFailAuth()
        fun onAuthError()
        fun onAcquired(info:Int)
    }

    companion object {
        private val TAG = "${NubiaSystemUIApplication.TAG}.NubMonitor"
        private  var mUpdateMonitor:NubiaBiometricMonitor? = null
            get(){
                if (field == null){
                    field = NubiaBiometricMonitor()
                }
                return field
            }

        public fun get():NubiaBiometricMonitor{
            return mUpdateMonitor!!
        }
    }
    private val mList = mutableListOf<Reference<UpdateMonitorCallback>>()

    fun addCallback(callback: UpdateMonitorCallback?){
        callback.apply {
            mHandler.post{
                if(mList.find { it.get()== this } == null){
                    mList.add(WeakReference(this))
                }
                removeCallback(null)
                Log.i(TAG, "addCallback size=${mList.size}")
            }
        }
    }

    fun removeCallback(callback: UpdateMonitorCallback?){
        mHandler.post{
            mList.removeAll { it.get() == callback }
        }
    }

    fun callStartAuth(owner: String?) {
        mHandler.post{
            mList.forEach{
                it.get()?.onStartAuth(owner)
            }
        }
    }

    fun callDoneAuth() {
        mHandler.post{
            mList.forEach{
                it.get()?.onDoneAuth()
            }
        }
    }

    fun callStopAuth() {
        mHandler.post{
            mList.forEach{
                it.get()?.onStopAuth()
            }
        }
    }

    fun callAuthError() {
        mHandler.post{
            mList.forEach{
                it.get()?.onAuthError()
            }
        }
    }

    fun callFailAuth() {
        mHandler.post{
            mList.forEach{
                it.get()?.onFailAuth()
            }
        }
    }

    fun callAcquired(info: Int) {
        mHandler.post{
            mList.forEach{
                it.get()?.onAcquired(info)
            }
        }
    }

    override fun dump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?){

    }
}