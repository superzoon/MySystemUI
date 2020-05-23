package cn.nubia.systemui.fingerprint.process

import android.content.Context
import cn.nubia.systemui.fingerprint.FingerprintController
import cn.nubia.systemui.fingerprint.FingerprintWindowController
import java.io.FileDescriptor
import java.io.PrintWriter


class ScreenOnProcess(mContext: Context, mController: FingerprintController,mWindowController: FingerprintWindowController):
        FingerprintProcess(mContext, mController, mWindowController) {
    override fun getFingerDownDelay(): Long {
        return 10;
    }

    override fun getFingerUIReadyDelay(): Long {
        return 30;
    }

    override fun getFingerUpDelay(): Long {
        return 0;
    }

    override fun dump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?) {
    }
}