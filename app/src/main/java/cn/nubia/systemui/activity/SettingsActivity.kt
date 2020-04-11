package cn.nubia.systemui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import cn.nubia.systemui.aidl.INubiaSystemUI
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : Activity(),ServiceConnection {
    val TAG = "${NubiaSystemUIApplication.TAG}.Activity"
    var mSystemUI:INubiaSystemUI?  = null;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        startService(Intent(this.applicationContext, NubiaSystemUIService::class.java))
        bindService(Intent(this.applicationContext, NubiaSystemUIService::class.java), this, Context.BIND_AUTO_CREATE)
        // Example of a call to a native method
        //sample_text.text = stringFromJNI()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        Log.i(TAG, "on connect ${mSystemUI}")
        mSystemUI = null
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        mSystemUI = INubiaSystemUI.Stub.asInterface(service)
        Log.i(TAG, "on connect ${mSystemUI}")
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(this)
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
        //    System.loadLibrary("native-lib")
        }
    }
}
