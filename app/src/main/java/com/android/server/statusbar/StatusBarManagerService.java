package com.android.server.statusbar;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Rect;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class StatusBarManagerService {
    Context mContext;

    private static final int TYPE_INIT = 0;
    private static final int TYPE_IME_VIS = 1;
    private static final int TYPE_WINDOW_VISIBLE = 2;
    private static final int TYPE_WINDOW_GONE = 3;
    private static final int TYPE_POWER_MODE = 4;
    private static final int TYPE_FOCUES_WINDOW = 5;
    private static final int TYPE_FP_START = 6;
    private static final int TYPE_FP_STOP = 7;
    private static final int TYPE_FP_ACQUIRED_INFO = 8;
    private static final int TYPE_FP_ERROR = 9;
    private static final int TYPE_FP_AUTHENTICATED = 10;
    private static final int TYPE_FP_ENROLL_RESULT = 11;
    private static final int TYPE_FP_ENROLL_START = 12;
    private static final int TYPE_FP_ENROLL_STOP = 13;
    private static final int TYPE_FP_ENROLL_ERROR = 14;

    private static final String KEY_IME_VISIBLE = "ImeVisible";
    private static final String KEY_WINDOW_VISIBLE = "WindowVisible";
    private static final String KEY_WINDOW_GONE = "WindowGone";
    private static final String KEY_POWER_MODE = "PowerMode";
    private static final String KEY_FOCUES_WINDOW_PACKAGE_NAME = "FocuesWindowPackageName";
    private static final String KEY_FOCUES_WINDOW_VIS_RECT = "FocuesWindowVisRect";
    private static final String KEY_ACCEPT = "Accept";
    private static final String KEY_AUTH_OWNER = "AuthOwner";
    private static final String KEY_ACQUIRED_INFO = "AcquiredInfo";
    private static final String KEY_DEVICE_ID = "DeviceId";
    private static final String KEY_ERROR = "Error";
    private static final String KEY_VENDOR_CODE = "VendorCode";
    private static final String KEY_START_AUTH = "StartAuthenticated";
    private static final String KEY_REMAINING = "Remaining";

    public static class FpUIStub extends Binder{
        public static final String TAG = "FpManager.UIStub";
        public static final String DESCRIPTOR = "systemui.FpUI";

        private FpUIStub(){}
        private static FpUIStub mStub = null;
        private boolean mImeVisible = false;
        private ComponentName mCurrentActivity = null;
        private int mPowerMode = -1;
        private String mFocuesWindowPackageName = null;
        private Rect mFocuesWindowVisRect = null;
        private boolean mStartAuth = false;
        private String mAuthOwner = null;

        interface Callback{
            default void onRegisterSucced(boolean startAuth, String fpOwner){}
            default void onImeVis(boolean vis){}
            default void onWindowVisible(ComponentName actvity){}
            default void onWindowGone(ComponentName actvity){}
            default void onPowerMode(int mode){}
            default void onFocuesWindow(String packageName, Rect visRect){}
            default void onFpStart(boolean startAuth, String owner){}
            default void onFpStop(boolean startAuth, String owner){}
            default void onFpAcquiredInfo(int info){}
            default void onFpError(long deviceId, int error, int vendorCode){}
            default void onFpAuthenticated(boolean authenticated){}
            default void onFpEnrollResult(int remaining){}
            default void onFpEnrollStart(){}
            default void onFpEnrollStop(){}
            default void onFpEnrollError(int error){}
        }

        private Callback mCallback = null;
        private Handler mHandler = null;

        public static void registerFpUI(final StatusBarManagerService service, final Callback callback ,final Handler handler){
            if(mStub==null && callback!=null && handler!=null){
                handler.post(()->{
                    mStub.mCallback = callback;
                    mStub.mHandler = handler;
                    Bundle bundle = new Bundle();
                    bundle.putBinder("token", mStub);
                    service.systemUIChannel("registerFpUI", bundle);
                });
            }else {
                Log.e(TAG, "you are register");
            }
        }


        private void initFpUI(Bundle data){
            if(data!=null){
                if(data.containsKey(KEY_START_AUTH)){
                    mStartAuth = data.getBoolean(KEY_START_AUTH);
                }
                if(data.containsKey(KEY_AUTH_OWNER)){
                    mAuthOwner = data.getString(KEY_AUTH_OWNER);
                }
            }
            mCallback.onRegisterSucced(mStartAuth, mAuthOwner);
        }

        public void imeVis(boolean vis){
            mCallback.onImeVis(vis);
        }

        public void windowVisible(ComponentName actvity){
            mCurrentActivity = actvity;
            mCallback.onWindowVisible(actvity);
        }

        public void windowGone(ComponentName actvity){
            mCallback.onWindowGone(actvity);
        }

        public void powerMode(int mode){
            mPowerMode = mode;
            mCallback.onPowerMode(mode);
        }

        public void focuesWindow(String packageName, Rect visRect){
            mFocuesWindowPackageName = packageName;
            mFocuesWindowVisRect = visRect;
            mCallback.onFocuesWindow(packageName, visRect);
        }

        public void fpStart(boolean startAuth, String owner){
            mStartAuth = startAuth;
            mAuthOwner = owner;
            mCallback.onFpStart(startAuth, owner);
        }

        public void fpStop(boolean startAuth, String owner){
            if(owner == null || !owner.equals(mAuthOwner)){
                Log.w(TAG, "fpStop owner="+owner);
            }
            mStartAuth = startAuth;
            mCallback.onFpStop(startAuth, owner);
        }

        public void fpAcquiredInfo(int info){
            mCallback.onFpAcquiredInfo(info);
        }

        public void fpError(long deviceId, int error, int vendorCode){
            mCallback.onFpError(deviceId, error, vendorCode);
        }

        public void fpAuthenticated(boolean authenticated){
            mCallback.onFpAuthenticated(authenticated);
        }

        public void fpEnrollResult(int remaining){
            mCallback.onFpEnrollResult(remaining);
        }

        public void fpEnrollStart(){
            mCallback.onFpEnrollStart();
        }

        public void fpEnrollStop(){
            mCallback.onFpEnrollStop();
        }

        private void fpEnrollError(int error){
            mCallback.onFpEnrollError(error);
        }

        private void onDataChange(final int type, final Bundle data){
            mHandler.post(()->{
                switch (type){
                    case TYPE_INIT:
                        initFpUI(data);
                        break;
                    case TYPE_IME_VIS:
                        if(data!=null && data.containsKey(KEY_IME_VISIBLE)){
                            imeVis(data.getBoolean(KEY_IME_VISIBLE));
                        }else{
                            Log.w(TAG, "has ime vis="+data.containsKey(KEY_IME_VISIBLE));
                        }
                        break;
                    case TYPE_WINDOW_VISIBLE:
                        if(data!=null && data.containsKey(KEY_WINDOW_VISIBLE)){
                            ComponentName name = data.getParcelable(KEY_WINDOW_VISIBLE);
                            windowVisible(name);
                        }else{
                            Log.w(TAG, "has window vis="+data.containsKey(KEY_WINDOW_VISIBLE));
                        }
                        break;
                    case TYPE_WINDOW_GONE:
                        if(data!=null && data.containsKey(KEY_WINDOW_GONE)){
                            ComponentName name = data.getParcelable(KEY_WINDOW_GONE);
                            windowGone(name);
                        }else{
                            Log.w(TAG, "has window gone="+data.containsKey(KEY_WINDOW_GONE));
                        }
                        break;
                    case TYPE_POWER_MODE:
                        if(data!=null && data.containsKey(KEY_POWER_MODE)){
                            powerMode(data.getInt(KEY_POWER_MODE));
                        }else{
                            Log.w(TAG, "has power mode="+data.containsKey(KEY_POWER_MODE));
                        }
                        break;
                    case TYPE_FOCUES_WINDOW:
                        if(data!=null && data.containsKey(KEY_FOCUES_WINDOW_PACKAGE_NAME)
                                && data.containsKey(KEY_FOCUES_WINDOW_VIS_RECT)){
                            Rect visRect = data.getParcelable(KEY_FOCUES_WINDOW_VIS_RECT);
                            focuesWindow(data.getString(KEY_FOCUES_WINDOW_PACKAGE_NAME), visRect);
                        }else{
                            Log.w(TAG, "has packageName="+data.containsKey(KEY_FOCUES_WINDOW_PACKAGE_NAME)
                                    +" has rect="+ data.containsKey(KEY_FOCUES_WINDOW_VIS_RECT));
                        }
                        break;
                    case TYPE_FP_START:
                        if(data!=null && data.containsKey(KEY_START_AUTH)
                                && data.containsKey(KEY_AUTH_OWNER)){
                            fpStart(data.getBoolean(KEY_START_AUTH), data.getString(KEY_AUTH_OWNER));
                        }else{
                            Log.w(TAG, "has StartAuth="+data.containsKey(KEY_START_AUTH)+" has owner="+ data.containsKey(KEY_AUTH_OWNER));
                        }
                        break;
                    case TYPE_FP_STOP:
                        if(data!=null && data.containsKey(KEY_START_AUTH)
                                && data.containsKey(KEY_AUTH_OWNER)){
                            fpStop(data.getBoolean(KEY_START_AUTH), data.getString(KEY_AUTH_OWNER));
                        }else{
                            Log.w(TAG, "has StartAuth="+data.containsKey(KEY_START_AUTH)+" has owner="+ data.containsKey(KEY_AUTH_OWNER));
                        }
                        break;
                    case TYPE_FP_ACQUIRED_INFO:
                        if(data!=null && data.containsKey(KEY_ACQUIRED_INFO)){
                            fpAcquiredInfo(data.getInt(KEY_ACQUIRED_INFO));
                        }else{
                            Log.w(TAG, "has acquired info="+data.containsKey(KEY_ACQUIRED_INFO));
                        }
                        break;
                    case TYPE_FP_ERROR:
                        if(data!=null && data.containsKey(KEY_DEVICE_ID)
                                && data.containsKey(KEY_ERROR)
                                && data.containsKey(KEY_VENDOR_CODE)){
                            fpError(data.getLong(KEY_DEVICE_ID), data.getInt(KEY_ERROR), data.getInt(KEY_VENDOR_CODE));
                        }else{
                            Log.w(TAG, "has device id="+data.containsKey(KEY_DEVICE_ID)+"has error="+
                                    data.containsKey(KEY_ERROR)+"has vendor code="+data.containsKey(KEY_VENDOR_CODE));
                        }
                        break;
                    case TYPE_FP_AUTHENTICATED:
                        if(data!=null && data.containsKey(KEY_ACCEPT)){
                            fpAuthenticated(data.getBoolean(KEY_REMAINING));
                        }else{
                            Log.w(TAG, "has accept="+data.containsKey(KEY_ACCEPT));
                        }
                        break;
                    case TYPE_FP_ENROLL_RESULT:
                        if(data!=null && data.containsKey(KEY_REMAINING)){
                            fpEnrollResult(data.getInt(KEY_REMAINING));
                        }else{
                            Log.w(TAG, "has remaining="+data.containsKey(KEY_REMAINING));
                        }
                        break;
                    case TYPE_FP_ENROLL_START:
                        fpEnrollStart();
                        break;
                    case TYPE_FP_ENROLL_STOP:
                        fpEnrollStop();
                        break;
                    case TYPE_FP_ENROLL_ERROR:
                        if(data!=null && data.containsKey(KEY_ERROR)){
                            fpEnrollError(data.getInt(KEY_ERROR));
                        }else{
                            Log.w(TAG, "has error="+data.containsKey(KEY_ERROR));
                        }
                        break;
                }
            });
        }

        @Override
        protected boolean onTransact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case INTERFACE_TRANSACTION: {
                    reply.writeString(DESCRIPTOR);
                    return true;
                }
                case IBinder.FIRST_CALL_TRANSACTION: {
                    data.enforceInterface(DESCRIPTOR);
                    int type = data.readInt();
                    android.os.Bundle bundle;
                    if ((0 != data.readInt())) {
                        bundle = android.os.Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    onDataChange(type, bundle);
                    return true;
                }
            }
            return super.onTransact(code, data, reply, flags);
        }

        public static void dump(PrintWriter writer, String prefix){
            if(mStub!=null){
                StringBuilder sb = new StringBuilder();
                sb.append(prefix);
                sb.append("FpUIStub:\n");
                sb.append(prefix);
                sb.append("    mImeVisible=");
                sb.append(mStub.mImeVisible);
                sb.append("\n");
                sb.append(prefix);
                sb.append("    mCurrentActivity=");
                sb.append(mStub.mCurrentActivity);
                sb.append("\n");
                sb.append(prefix);
                sb.append("    mPowerMode=");
                sb.append(mStub.mPowerMode);
                sb.append("\n");
                sb.append(prefix);
                sb.append("    mFocuesWindowPackageName=");
                sb.append(mStub.mFocuesWindowPackageName);
                sb.append("\n");
                sb.append(prefix);
                sb.append("    mFocuesWindowVisRect=");
                sb.append(mStub.mFocuesWindowVisRect);
                sb.append("\n");
                sb.append(prefix);
                sb.append("    mStartAuth=");
                sb.append(mStub.mStartAuth);
                sb.append("\n");
                sb.append(prefix);
                sb.append("    mAuthOwner=");
                sb.append(mStub.mAuthOwner);
                sb.append("\n");
                writer.write(sb.toString());
            }
        }
    }

    public static class FpUIProxy implements IBinder.DeathRecipient {
        public static final String TAG = "FpManager.UIProxy";
        public static final String DESCRIPTOR = "systemui.FpUI";

        private static final FpUIProxy mProxy = new FpUIProxy();
        private final List<String> mOwnerList = new ArrayList<>();
        private FpUIProxy(){}
        private IBinder mToken;
        private int mPid;
        private int mUid;
        private final Bundle mBundle = new Bundle();

        public static boolean setToken(IBinder token, int pid, int uid){
            return mProxy._setToken(token, pid, uid);
        }

        private void initFpUI(){
            callFpUI(TYPE_INIT, mBundle);
        }

        public static void imeVis(boolean vis){
            synchronized (mProxy){
                mProxy.mBundle.putBoolean(KEY_IME_VISIBLE, vis);
            }
            callFpUI(TYPE_IME_VIS, mProxy.mBundle);
        }

        public static void windowVisible(ComponentName actvity){
            Bundle bundle = new Bundle();
            bundle.putParcelable(KEY_WINDOW_VISIBLE, actvity);
            callFpUI(TYPE_WINDOW_VISIBLE, mProxy.mBundle);
        }

        public static void windowGone(ComponentName actvity){
            Bundle bundle = new Bundle();
            bundle.putParcelable(KEY_WINDOW_GONE, actvity);
            callFpUI(TYPE_WINDOW_GONE, mProxy.mBundle);
        }

        public static void powerMode(int type){
            synchronized (mProxy){
                mProxy.mBundle.putInt(KEY_POWER_MODE, type);
            }
            callFpUI(TYPE_POWER_MODE, mProxy.mBundle);
        }

        public static void focuesWindow(String packageName, Rect visRect){
            synchronized (mProxy){
                mProxy.mBundle.putString(KEY_FOCUES_WINDOW_PACKAGE_NAME, packageName);
                Rect mFocuesWindowVisRect = mProxy.mBundle.getParcelable(KEY_FOCUES_WINDOW_VIS_RECT);
                mProxy.mBundle.putParcelable(KEY_FOCUES_WINDOW_VIS_RECT, new Rect(visRect));
            }
            callFpUI(TYPE_FOCUES_WINDOW, mProxy.mBundle);
        }

        public static void fpStart(String owner){
            synchronized (mProxy){
                if(!mProxy.mOwnerList.contains(owner)){
                    mProxy.mOwnerList.add(owner);
                }
                mProxy.mBundle.putBoolean(KEY_START_AUTH, !mProxy.mOwnerList.isEmpty());
                mProxy.mBundle.putString(KEY_AUTH_OWNER, owner);
            }
            callFpUI(TYPE_FP_START, mProxy.mBundle);
        }

        public static void fpStop(String owner){
            synchronized (mProxy){
                if(mProxy.mOwnerList.contains(owner)){
                    mProxy.mOwnerList.remove(owner);
                }
                mProxy.mBundle.putBoolean(KEY_START_AUTH, !mProxy.mOwnerList.isEmpty());
                mProxy.mBundle.putString(KEY_AUTH_OWNER, owner);
            }
            callFpUI(TYPE_FP_STOP, mProxy.mBundle);
        }

        public static void fpAcquiredInfo(int info){
            Bundle bundle = new Bundle();
            bundle.putInt(KEY_ACQUIRED_INFO, info);
            callFpUI(TYPE_FP_ACQUIRED_INFO, bundle);
        }

        public static void fpError(long deviceId, int error, int vendorCode){
            Bundle bundle = new Bundle();
            bundle.putLong(KEY_DEVICE_ID, deviceId);
            bundle.putInt(KEY_ERROR, error);
            bundle.putInt(KEY_VENDOR_CODE, vendorCode);
            callFpUI(TYPE_FP_ERROR, bundle);
        }

        public static void fpAuthenticated(boolean authenticated){
            Bundle bundle = new Bundle();
            bundle.putBoolean(KEY_ACCEPT, authenticated);
            callFpUI(TYPE_FP_AUTHENTICATED, bundle);
        }

        public static void fpEnrollResult(int remaining){
            Bundle bundle = new Bundle();
            bundle.putInt(KEY_REMAINING, remaining);
            callFpUI(TYPE_FP_ENROLL_RESULT, bundle);
        }

        public static void fpEnrollStart(){
            callFpUI(TYPE_FP_ENROLL_START, null);
        }

        public static void fpEnrollStop(){
            callFpUI(TYPE_FP_ENROLL_STOP, null);
        }

        public static void fpEnrollError(int error){
            Bundle bundle = new Bundle();
            bundle.putInt(KEY_ERROR, error);
            callFpUI(TYPE_FP_ENROLL_ERROR, null);
        }

        private static void callFpUI(int type, android.os.Bundle data) {
            try {
                mProxy._callFpUI(type, data);
            } catch (RemoteException e) {
                Log.w(TAG, "callFpUI err :"+e.getMessage());
            }
        }

        private boolean _setToken(IBinder token, int pid, int uid){
            if(token!=null && token.isBinderAlive()){
                try {
                    token.linkToDeath(this, 0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    return false;
                }
                if(mToken!=null && mToken.isBinderAlive()){
                    mToken.unlinkToDeath(this, 0);
                }
                mToken = token;
                mPid = pid;
                mUid = uid;
                initFpUI();
                return true;
            }
            return false;
        }

        private void _callFpUI(int type, android.os.Bundle data) throws android.os.RemoteException {
            if(mToken!=null && mToken.isBinderAlive()){
                android.os.Parcel _data = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(type);
                    if ((data != null)) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    mToken.transact(IBinder.FIRST_CALL_TRANSACTION, _data, null, android.os.IBinder.FLAG_ONEWAY);
                } finally {
                    _data.recycle();
                }
            }
        }
        @Override
        public void binderDied() {
            mToken = null;
            Log.w(TAG, "binderDied PID="+mPid+" UID="+mUid);
        }
    }
    private boolean registerFpUI(Bundle data, int pid, int uid){
        if(data.containsKey("token")){
            IBinder token = data.getBinder("token");
        }
        return false;
    }

    private Bundle addPidUid(Bundle data, int pid, int uid){
        if(data == null){
            data = new Bundle();
        }
        if(!data.containsKey("_pid")){
            data.putInt("_pid", pid);
        }
        if(!data.containsKey("_uid")){
            data.putInt("_uid", pid);
        }
        return data;
    }

    public void systemUIChannel(String action, Bundle data){
        final int pid = Binder.getCallingPid();
        final int uid = Binder.getCallingUid();
        addPidUid(data, pid, uid);
        if("registerFpUI".equals(action) && registerFpUI(data, pid, uid)){
            return ;
        }
    }
}
