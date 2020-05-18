package cn.nubia.systemui.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PackageReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action){
            Intent.ACTION_PACKAGE_ADDED -> UpdateMonitor.get().callPackageChange(true, intent.data)
            Intent.ACTION_PACKAGE_REMOVED -> UpdateMonitor.get().callPackageChange(false, intent.data)
        }
    }
}