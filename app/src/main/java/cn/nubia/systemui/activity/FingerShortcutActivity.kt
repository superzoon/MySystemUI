package cn.nubia.systemui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.Toast
import cn.nubia.systemui.NubiaSystemUIApplication
import cn.nubia.systemui.fingerprint.view.FingerShortcutView
import java.util.ArrayList
import cn.nubia.systemui.R
import cn.nubia.systemui.common.InputProxy

class FingerShortcutActivity : Activity() {
    var mIndex = 2
    companion object {
        val TAG = "${NubiaSystemUIApplication.TAG}.Shortcut"
    }
    val entitys: ArrayList<FingerShortcutView.ShortcutEntity<Intent>>
        get() {
            val entitys = ArrayList<FingerShortcutView.ShortcutEntity<Intent>>()
            val quicklyIconSmallIds = intArrayOf(R.drawable.finger_quickly_wxccm_small,
                    R.drawable.finger_quickly_wxfkm_small,
                    R.drawable.finger_quickly_wxsys_small,
                    R.drawable.finger_quickly_zfbewm_small,
                    R.drawable.finger_quickly_zfbsys_small)
            val quicklyNmaeIds = intArrayOf(R.string.finger_quickly_wxccf,
                    R.string.finger_quickly_wxfkm,
                    R.string.finger_quickly_wxsys,
                    R.string.finger_quickly_zfbewm,
                    R.string.finger_quickly_zfbsys)
            for (i in quicklyNmaeIds.indices) {
                val intent = Intent()
                intent.setClass(this, FingerShortcutActivity::class.java!!)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                entitys.add(FingerShortcutView.ShortcutEntity(quicklyIconSmallIds[i], quicklyNmaeIds[i], intent))
            }
            return entitys
        }
    val EventListener = object :InputProxy.EventListener{
        override fun onTouchEvent(event: MotionEvent) {
            Log.e(TAG, "onTouchEvent ${event}")
        }

        override fun onKeyEvent(event: KeyEvent) {
            Log.e(TAG, "onKeyEvent ${event}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "onDestroy")

        InputProxy.get(this).unregisterEventListener(EventListener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(TAG, "onCreate")
        setContentView(R.layout.finger_shortcut_test_activity)
        val t = HandlerThread("bg")
        t.start()
        InputProxy.get(this).registerEventListener(EventListener)
        val view: FingerShortcutView = findViewById(R.id.qucikly_enter_view)!!
        view.setEntity(mIndex, entitys, Handler(t.looper)).setCallback(object : FingerShortcutView.Callback {
            override fun onSelect(mView: FingerShortcutView, index: Int, entity: FingerShortcutView.ShortcutEntity<*>?) {
                mIndex = index
                Toast.makeText(this@FingerShortcutActivity, "onSelect " + resources.getString(entity!!.textId), Toast.LENGTH_LONG).show()
                //                Intent intent = entity.getTag();
                //                intent.putExtra("name",getResources().getString(entity.getTextId()));
                //                startActivity(entity.getTag());
                finish()
            }

            override fun onSlideTo(mView: FingerShortcutView, index: Int, entity: FingerShortcutView.ShortcutEntity<*>?) {
            }

            override fun onCancel() {
                Toast.makeText(this@FingerShortcutActivity, "onCancel", Toast.LENGTH_LONG).show()
                finish()
            }
        })
    }
}
