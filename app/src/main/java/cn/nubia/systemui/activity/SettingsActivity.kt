package cn.nubia.systemui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.graphics.drawable.VectorDrawable
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import cn.nubia.systemui.activity.FingerShortcutActivity
import cn.nubia.systemui.activity.GLRender
import cn.nubia.systemui.aidl.INubiaSystemUI
import cn.nubia.systemui.common.BiometricConstant
import java.nio.charset.Charset

class SettingsActivity : Activity(),ServiceConnection, View.OnClickListener{
    fun sendBiometricInfo(info:String){
        mSystemUI?.onBiometricChange(BiometricConstant.TYPE_INFO, Bundle().apply {
            putString("info",info)
        });
        if(mSystemUI==null){
            Log.i(TAG, "mSystemUI==null")
        }
    }
    override fun onClick(v: View) {

        Log.i(TAG, "onClick ${resources.getResourceName(v.id)}")

        when(v.id){
            R.id.sample_button -> startActivity(Intent(this, FingerShortcutActivity::class.java))
            R.id.start_fingerprint -> {
                bindService(Intent(this.applicationContext, NubiaSystemUIService::class.java), this, Context.BIND_AUTO_CREATE)
            }
            R.id.stop_auth_fingerprint -> {
                sendBiometricInfo("stopAuth")
                unbindService(this)
            }
            R.id.done_auth_fingerprint -> {
                sendBiometricInfo("doneAuth")
            }
            R.id.auth_error_fingerprint -> {
                sendBiometricInfo("autherror")
            }
            R.id.fail_auth_fingerprint -> {
                sendBiometricInfo("failAuth")
            }
            R.id.error_4_fingerprint -> {
                sendBiometricInfo("4")
            }
            R.id.error_5_fingerprint -> {
                sendBiometricInfo("5")
            }
            R.id.error_20_fingerprint -> {
                sendBiometricInfo("1120")
            }
        }
    }

    val TAG = "${NubiaSystemUIApplication.TAG}.Activity"
    var mSystemUI:INubiaSystemUI?  = null;

    val DEBUG = true
    var mGLRender:GLRender? = null
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
        setContentView(R.layout.activity_settings)
        if(DEBUG){
            startService(Intent(this.applicationContext, NubiaSystemUIService::class.java))
        }
        mGLRender = GLRender(findViewById(R.id.surface_view))
        // Example of a call to a native method
        //sample_text.text = stringFromJNI()
        (getDrawable(R.drawable.ic_launcher_background) as VectorDrawable).apply {

        }
    }

    override fun onResume() {
        super.onResume()
        mGLRender?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mGLRender?.onPause()
    }

    override fun onStart() {
        super.onStart()
       
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        Log.i(TAG, "on onServiceDisconnected ${mSystemUI}")
        mSystemUI = null
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        mSystemUI = INubiaSystemUI.Stub.asInterface(service)
        Log.i(TAG, "on onServiceConnected ${mSystemUI}")
        sendBiometricInfo("startAuth_com.android.systemui")
    }

    override fun onDestroy() {
        super.onDestroy()
        if(DEBUG){
            unbindService(this)
        }
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
