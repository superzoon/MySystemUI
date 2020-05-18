package cn.nubia.systemui.common

import android.content.Context
import android.os.Handler
import android.os.PowerManager
import android.os.SystemClock
import android.os.SystemProperties
import android.util.Log
import android.view.Display
import cn.nubia.systemui.NubiaSystemUIApplication
import cn.nubia.systemui.NubiaSystemUIService
import cn.nubia.systemui.NubiaThreadHelper

class PowerController(mContext: Context): Controller(mContext), UpdateMonitor.UpdateMonitorCallback {
    private var mDisplayState:Int=0
    private var mWakeUpName:String?=null
    private val mPowerManager = mContext.getSystemService(PowerManager::class.java)
    private val mWakeLockMap = mutableMapOf<String, PowerManager.WakeLock>()
    override fun getHandler(): Handler {
        return NubiaThreadHelper.get().getBgHander()
    }

    override fun onStart(service: NubiaSystemUIService) {
        handlerInvoke {
            UpdateMonitor.get().let {
                it.addCallback(this)
                mDisplayState = it.getDisplayState()
            }
        }
    }

    override fun onStop(service: NubiaSystemUIService) { }

    override fun onDisplayChange(displayId: Int, state: Int, stateStr: String) {
        super.onDisplayChange(displayId, state, stateStr)
        if(displayId == Display.DEFAULT_DISPLAY){
            mDisplayState = state;
        }
    }

    fun isScreenOn() = mDisplayState==Display.STATE_ON

    fun isScreenOff() = mDisplayState==Display.STATE_OFF

    fun isScreenDoze() = mDisplayState==Display.STATE_DOZE

    fun isSuspendedState(): Boolean {
        return mDisplayState == Display.STATE_OFF || mDisplayState == Display.STATE_DOZE_SUSPEND
    }
    fun isInteractive():Boolean = !isSuspendedState();

    fun isFingerWakeUp() = FINGER_WAKE_UP_KEY==mWakeUpName

    fun fingerWakeup(){
        handlerInvoke {
            mWakeUpName = FINGER_WAKE_UP_KEY
            val WAKE_REASON_GENSTURE :Int = 4
            PowerManager::class.java.getDeclaredMethod("wakeUp",Long::class.java, Int::class.java, String::class.java)
                    .invoke(mPowerManager, SystemClock.uptimeMillis(), WAKE_REASON_GENSTURE, mWakeUpName)
        }
    }

    fun wakeUp(levelAndFlags:Int, key:String, timeout:Long=0){
        handlerInvoke {
            mWakeUpName = key
            if(!mWakeLockMap.containsKey(key)){
                mWakeLockMap[key] = mPowerManager.newWakeLock(levelAndFlags, key)
                mWakeLockMap[key]!!.acquire()
                if(timeout>0){
                    getHandler().postDelayed({release(key)}, timeout)
                }
            }else{
                Log.w(TAG,"not release, so return")
            }
        }
    }

    fun release(key:String){
        handlerInvoke {
            if(mWakeLockMap.containsKey(key)){
                mWakeLockMap[key]!!.release()
                mWakeLockMap.remove(key)
            }
        }
    }

    override fun onStartGoingToSleep(reason: Int) {
        super.onStartGoingToSleep(reason)
        handlerInvoke {
            mWakeUpName = null
        }
    }

    fun gotoAodMode(){
        if(!isScreenDoze()){

        }
    }

    fun gotoSleepMode(){
        if(!isScreenOff()){

        }
    }

    fun gotoWakeupMode(){
        if(!isScreenOn()){

        }
    }

    companion object {
        private val TAG = "${NubiaSystemUIApplication.TAG}.PowerController"
        private val FINGER_WAKE_UP_KEY = "SystUIFingerKey"
        val isSupportFpWakeup:Boolean = SystemProperties.getInt("sys.nubia.fpmopde.private", 0) == 1
    }
}