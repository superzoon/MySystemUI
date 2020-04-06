// INubiaSystemUI.aidl
package cn.nubia.systemui.aidl;

import cn.nubia.systemui.aidl.ISystemUI;
// Declare any non-default types here with import statements

interface INubiaSystemUI {
    void onConnect(in ISystemUI systemui);
}
