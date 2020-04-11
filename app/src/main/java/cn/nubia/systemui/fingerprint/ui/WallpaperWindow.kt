package cn.nubia.systemui.fingerprint.ui

import android.content.Intent
import android.service.wallpaper.WallpaperService

class WallpaperWindow:WallpaperService(){

    override fun onCreate() {
        super.onCreate()
//       var x:IWallpaperServiceWrapper =  onBind(null)
    }

    override fun onCreateEngine(): Engine? {
        return object :Engine(){

        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}