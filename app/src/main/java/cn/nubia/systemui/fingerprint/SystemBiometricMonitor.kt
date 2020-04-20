package cn.nubia.systemui.fingerprint

import android.hardware.biometrics.IBiometricServiceReceiverInternal
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import cn.nubia.systemui.NubiaSystemUIApplication
import java.lang.ref.Reference
import java.lang.ref.WeakReference

class SystemBiometricMonitor private constructor() {
    private val mHandler = Handler(Looper.getMainLooper());
    interface UpdateMonitorCallback{
        fun showBiometricView(bundle: Bundle?, receiver: IBiometricServiceReceiverInternal?, type: Int, requireConfirmation: Boolean, userId: Boolean){}
        fun hideBiometricView(){}
        fun onBiometricAuthenticated(authenticated: Boolean, failureReason: String?){}
        fun onBiometricHelp(message: String?){}
        fun onBiometricError(error: String?){}
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

    fun callShowBiometricView(bundle: Bundle?, receiver: IBiometricServiceReceiverInternal?, type: Int, requireConfirmation: Boolean, userId: Boolean) {
        mHandler.post{
            mList.forEach{
                it.get()?.showBiometricView(bundle, receiver, type, requireConfirmation, userId)
            }
        }
    }

    fun callHideBiometricView() {
        mHandler.post{
            mList.forEach{
                it.get()?.hideBiometricView()
            }
        }
    }

    fun callBiometricAuthenticated(authenticated: Boolean, failureReason: String?) {
        mHandler.post{
            mList.forEach{
                it.get()?.onBiometricAuthenticated(authenticated, failureReason)
            }
        }
    }

    fun callBiometricHelp(message: String?) {
        mHandler.post{
            mList.forEach{
                it.get()?.onBiometricHelp(message)
            }
        }
    }

    fun callBiometricError(error: String?) {
        mHandler.post{
            mList.forEach{
                it.get()?.onBiometricError(error)
            }
        }
    }

    companion object {
        private val TAG = "${NubiaSystemUIApplication.TAG}.SysMonitor"
        private  var mUpdateMonitor:SystemBiometricMonitor? = null
            get(){
                if (field == null){
                    field = SystemBiometricMonitor()
                }
                return field
            }

        public fun get():SystemBiometricMonitor{
            return mUpdateMonitor!!
        }
    }
}