package cn.nubia.systemui.fingerprint.process

import android.content.Context
import cn.nubia.systemui.fingerprint.FingerprintController
import cn.nubia.systemui.fingerprint.FingerprintWindowController
import cn.nubia.systemui.fingerprint.action.ActionEvent
import java.io.FileDescriptor
import java.io.PrintWriter


class ScreenOffProcess(mContext: Context, mController: FingerprintController,mWindowController: FingerprintWindowController):
        FingerprintProcess(mContext, mController, mWindowController) {

    override fun triggerFingerDown(){
        super.triggerFingerDown()
        if(mState == ProcessState.DOWNING){
            mFpController.mActionEvent.addScreenOnAction(ActionEvent.Action("onFingerDown"){
                mFpController.onFingerDown()
            })
        }
    }

    override fun getFingerDownDelay(): Long {
        return 1000;
    }
    override fun getFingerUIReadyDelay(): Long {
        return 30;
    }

    override fun getFingerUpDelay(): Long {
        return 0;
    }

    override fun dump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?) {
        super.dump(fd, writer, args)
    }
}