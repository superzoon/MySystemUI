// INubiaSystemUI.aidl
package cn.nubia.systemui.aidl;

import android.os.Bundle;
// Declare any non-default types here with import statements

interface INubiaSystemUI {
    oneway void onConnect(in IBinder systemui);
    oneway void onSystemUIChange(int type, in Bundle data);
}
