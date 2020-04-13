// ISystemUI.aidl
package cn.nubia.systemui.aidl;

import android.os.Bundle;
// Declare any non-default types here with import statements

interface ISystemUI {
   oneway void setAodMode(int mode);
   void syncStartDozing();
   void syncStopDozing();
   oneway void setTouchPanelMode(int mode);
   oneway void setHbmMode(int mode);
   oneway void callFingerprintService(int type, in Bundle data);
   oneway void callSystemUI(int type, in Bundle data);
}
