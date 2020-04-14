package cn.nubia.systemui.common

import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import cn.nubia.systemui.aidl.ISystemUI

class SystemUI(private val mBinder: IBinder):ISystemUI{

    private val mSystemUI = ISystemUI.Stub.asInterface(mBinder)

    companion object {
        val AOD_MODE_DOZE = 1
        val AOD_MODE_SLEEP = 2
        val AOD_MODE_WAKE_UP = 3

        val HWC_POWER_MODE_OFF = 0
        val HWC_POWER_MODE_DOZE = 1
        val HWC_POWER_MODE_NORMAL = 2
        val HWC_POWER_MODE_DOZE_SUSPEND = 3

        val DISPLAY_STATE_SLEEP = 0
        val DISPLAY_STATE_WAKE_UP = 1
        val DISPLAY_STATE_DOZE = 2

        val POLICY_OFF = 0
        val POLICY_DOZE = 1
        val POLICY_DIM = 2
        val POLICY_BRIGHT = 3
        val POLICY_VR = 4
    }

    override fun setAodMode(mode:Int){
        try {
            mSystemUI.setAodMode(mode)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    override fun syncStartDozing() {
        try {
            mSystemUI.syncStartDozing()
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    override fun syncStopDozing() {
        try {
            mSystemUI.syncStopDozing()
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    override fun setTouchPanelMode(mode: Int) {
        try {
            mSystemUI.setTouchPanelMode(mode)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    override fun setHbmMode(mode: Int) {
        try {
            mSystemUI.setHbmMode(mode)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    override fun callFingerprintService(type: Int, data: Bundle?) {
        try {
            mSystemUI.callFingerprintService(type, data)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    override fun asBinder(): IBinder = mBinder

    override fun callSystemUI(type: Int, data: Bundle) {
        try {
            mSystemUI.callSystemUI(type, data)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }
}