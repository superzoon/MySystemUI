package android.view;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class SurfaceUtil {
    public static void copyFrom(Surface surface, SurfaceControl control){
        try {
            Surface.class.getDeclaredMethod("copyFrom", SurfaceControl.class).invoke(surface, control);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();

            Proxy.newProxyInstance(surface.getClass().getClassLoader(), new Class[]{}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    return null;
                }
            });
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
