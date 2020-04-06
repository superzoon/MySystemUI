package cn.nubia.systemui.common

import android.os.IBinder
import cn.nubia.systemui.aidl.ISystemUI

class SystemUI(private val mBinder: IBinder){
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

    fun setAodMode(mode:Int){
        mSystemUI.setAodMode(mode)
    }

}