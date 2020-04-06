package cn.nubia.systemui

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import cn.nubia.systemui.aidl.ISystemUI;
import cn.nubia.systemui.aidl.INubiaSystemUI;
import cn.nubia.systemui.common.SystemUI
import cn.nubia.systemui.ext.Controller
import cn.nubia.systemui.ext.UpdateMonitor
import java.io.FileDescriptor
import java.io.PrintWriter

class NubiaSystemUIService:Service(){
    val mNubiaSystemUI by lazy {
        NubiaSystemUI()
    }

    override fun onCreate() {
        super.onCreate()
        Controller.forEach { controller -> {
            controller.start(this)
        } }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Controller.forEach { controller -> {
            controller.onTrimMemory(level)
        } }
    }

    override fun dump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?) {
        super.dump(fd, writer, args)
        Controller.forEach { controller -> {
            controller.dump(fd, writer, args)
        } }
    }

    override fun onDestroy() {
        super.onDestroy()
        Controller.forEach { controller -> {
            controller.stop(this)
        } }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return mNubiaSystemUI
    }

    class NubiaSystemUI:INubiaSystemUI.Stub(), IBinder.DeathRecipient {
        override fun binderDied() {
            UpdateMonitor.get().callSystemUIDisConnect()
        }

        override fun onConnect(systemui: IBinder) {
            systemui.linkToDeath(this, 0)
            UpdateMonitor.get().callSystemUIConnect(SystemUI(systemui))
        }

        override fun onSystemUIChange(type:Int, data:Bundle) {
            UpdateMonitor.get().callSystemUIChange(type, data)
        }
    }
}