package cn.nubia.systemui.fingerprint.process

import android.content.Context
import cn.nubia.systemui.fingerprint.FingerprintController
import cn.nubia.systemui.fingerprint.FingerprintWindowController
import java.io.FileDescriptor
import java.io.PrintWriter


class ScreenOffProcess(mContext: Context, mController: FingerprintController,mWindowController: FingerprintWindowController):
        FingerprintProcess(mContext, mController, mWindowController) {

    override fun dump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?) {
        super.dump(fd, writer, args)
    }
}