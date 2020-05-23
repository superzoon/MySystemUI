package cn.nubia.systemui.fingerprint.process

import android.content.Context
import cn.nubia.systemui.fingerprint.FingerprintController
import cn.nubia.systemui.fingerprint.FingerprintWindowController
import cn.nubia.systemui.fingerprint.action.ActionEvent
import java.io.FileDescriptor
import java.io.PrintWriter


class ScreenAodProcess(mContext: Context, mFpController: FingerprintController,mWindowController: FingerprintWindowController):
        FingerprintProcess(mContext, mFpController, mWindowController) {

    override fun triggerFingerDown(){
        super.triggerFingerDown()
        if(mState == ProcessState.DOWNING){
            mFpController.mActionEvent.addScreenOnAction(ActionEvent.Action("onFingerDown"){
                mFpController.onFingerDown()
            })
        }
    }

    override fun getFingerDownDelay(): Long {
        return 100;
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