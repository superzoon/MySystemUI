package cn.nubia.systemui

import android.app.Service
import android.content.Intent
import android.os.IBinder

class NubiaSystemUIService:Service(){
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}