package cn.nubia.systemui.fingerprint.process

import android.content.Context
import cn.nubia.systemui.fingerprint.FingerprintController
import java.io.FileDescriptor
import java.io.PrintWriter


class ScreenOnProcess(mContext: Context, mController: FingerprintController): FingerprintProcess(mContext, mController)  {

    override fun dump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?) {
    }
}