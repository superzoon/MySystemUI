package cn.nubia.systemui

import android.app.Service
import android.content.Intent
import android.os.IBinder
import cn.nubia.systemui.aidl.ISystemUI;
import cn.nubia.systemui.aidl.INubiaSystemUI;
import cn.nubia.systemui.common.SystemUI
import cn.nubia.systemui.ext.UpdateMonitor

class NubiaSystemUIService:Service(){
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    class NubiaSystemUI:INubiaSystemUI.Stub(){
        override fun onConnect(systemui: ISystemUI) {
            UpdateMonitor.get().callSystemUIConnect(SystemUI(systemui))
        }

    }
}