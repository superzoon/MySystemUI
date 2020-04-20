// INubiaSystemUI.aidl
package cn.nubia.systemui.aidl;

import android.os.Bundle;
import android.content.ComponentName;
import java.lang.String;
// Declare any non-default types here with import statements

interface INubiaSystemUI {
    oneway void onConnect(in IBinder systemui);
    oneway void onFocusWindowChange(in ComponentName name);
    oneway void onStartActivity(in ComponentName name);
    oneway void onStopActivity(in ComponentName name);
    oneway void onBiometricChange(int type, in Bundle data);
    oneway void callNubiaSystemUI(int type, in Bundle data);
    oneway void onAodViewChange(boolean show);
    oneway void onKeyguardChange(boolean show, boolean occluded);
    oneway void onStartWakingUp();
    oneway void onFinishedWakingUp();
    oneway void onStartGoingToSleep(int reason);
    oneway void onFinishedGoingToSleep();
    oneway void onFingerprintKeycode(int keycode);
}
