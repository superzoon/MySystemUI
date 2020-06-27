package cn.nubia.systemui.common

import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.Settings
import android.util.Log
import cn.nubia.systemui.NubiaSystemUIApplication
import cn.nubia.systemui.NubiaThreadHelper
import java.lang.ref.Reference
import java.lang.ref.WeakReference

//监控settings值变化
class SwitchMonitor (val mHandler:Handler = NubiaThreadHelper.get().getFpBgHander()): ContentObserver(mHandler){

    interface Callback{
        open fun onGloablChange(key:String, value:String?){ }
        open fun onSystemChange(key:String, value:String?){ }
        open fun onSecureChange(key:String, value:String?){ }
    }

    private val mList = mutableListOf<Reference<Callback>>()

    private val mGloablNames = arrayListOf<String>("")
    private val mSystemNames = arrayListOf<String>("")
    private val mSecureNames = arrayListOf<String>("")
    private val mGloablMap = mutableMapOf<String, String?>()
    private val mSystemMap = mutableMapOf<String, String?>()
    private val mSecureMap = mutableMapOf<String, String?>()

    private val mContext by lazy {
        NubiaSystemUIApplication.getContext()
    }
    private val mResolver  = mContext.contentResolver

    fun addCallback(callback: Callback?){
        callback.apply {
            mHandler.post{
                if(mList.find { it.get()== this } == null){
                    mList.add(WeakReference(this))
                }
                removeCallback(null)
                Log.i(TAG, "addCallback size=${mList.size}")
            }
        }
    }

    fun removeCallback(callback: Callback?){
        mHandler.post{
            mList.removeAll { it.get() == callback }
        }
    }

    fun observer(){
        NubiaThreadHelper.get().handlerFpBg {
            mContext.contentResolver.also {resolver->
                mGloablNames.forEach{ name->
                    resolver.registerContentObserver(Settings.Global.getUriFor(name), false, this)
                    mGloablMap[name] = Settings.Global.getString(mResolver, name)
                    mList.forEach{ ref ->
                        ref.get()?.onGloablChange(name, mGloablMap[name])
                    }
                }
                mSystemNames.forEach{ name->
                    resolver.registerContentObserver(Settings.Global.getUriFor(name), false, this)
                    mSystemMap[name] = Settings.System.getString(mResolver, name)
                    mList.forEach{ ref ->
                        ref.get()?.onSystemChange(name, mSystemMap[name])
                    }
                }
                mSecureNames.forEach{ name->
                    resolver.registerContentObserver(Settings.Global.getUriFor(name), false, this)
                    mSecureMap[name] = Settings.Secure.getString(mResolver, name)
                    mList.forEach{ ref ->
                        ref.get()?.onSecureChange(name, mSecureMap[name])
                    }
                }
            }
        }
    }

    fun getGloablInt(key:String, default:Int):Int{
        return getGloablValue(key, "${default}").toInt()
    }

    fun getGloablValue(key:String, default:String):String{
        val value = if(key in mGloablMap){
            mGloablMap[key]
        }else{
            Settings.Global.getString(mResolver, key)
        }
        return if(value.isNullOrEmpty()){
            default
        }else{
            value!!
        }
    }

    fun getSystemInt(key:String, default:Int):Int{
        return getSystemValue(key, "${default}").toInt()
    }

    fun getSystemValue(key:String, default:String):String{
        val value = if(key in mSystemMap){
            mSystemMap[key]
        }else{
            Settings.System.getString(mResolver, key)
        }
        return if(value.isNullOrEmpty()){
            default
        }else{
            value!!
        }
    }

    fun getSecureInt(key:String, default:Int):Int{
        return getSecureValue(key, "${default}").toInt()
    }

    fun getSecureValue(key:String, default:String):String{
        val value = if(key in mSystemMap){
            mSystemMap[key]
        }else{
            Settings.System.getString(mResolver, key)
        }
        return if(value.isNullOrEmpty()){
            default
        }else{
            value!!
        }
    }

    val log = { mag:String ->
        if(true)Log.i(TAG, mag)
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        if(selfChange && uri!=null){
            mGloablNames.forEach{ name->
                if(Settings.Global.getUriFor(name).equals(uri)){
                    mGloablMap[name] = Settings.Global.getString(mResolver, name)
                    log("onChange uri=${uri} value= ${mGloablMap[name]}")
                }
            }
            mSystemNames.forEach{ name->
                if(Settings.System.getUriFor(name).equals(uri)){
                    mSystemMap[name] = Settings.System.getString(mResolver, name)
                    log("onChange uri=${uri} value= ${mSystemMap[name]}")
                }
            }
            mSecureNames.forEach{ name->
                if(Settings.Secure.getUriFor(name).equals(uri)){
                    mSecureMap[name] = Settings.Secure.getString(mResolver, name)
                    log("onChange uri=${uri} value= ${mSecureMap[name]}")
                }
            }
        }
    }

    companion object {
        private val TAG = "${NubiaSystemUIApplication.TAG}.Monitor"
        private val mSwitchMonitor:SwitchMonitor = SwitchMonitor()
        fun get():SwitchMonitor{
            return mSwitchMonitor
        }
    }
}