package android.view;

import android.graphics.Bitmap;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class SurfaceUtil {

    public static Surface createSurface(){
        try {
            return Surface.class.getConstructor().newInstance();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        Bitmap b = null;
        b.recycle();
        return null;
    }

    public static void copyFrom(Surface surface, SurfaceControl control){
        try {
            Surface.class.getDeclaredMethod("copyFrom", SurfaceControl.class).invoke(surface, control);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
    public static void createFrom(Surface surface, SurfaceControl control){
        try {
            Surface.class.getDeclaredMethod("createFrom", SurfaceControl.class).invoke(surface, control);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

}
