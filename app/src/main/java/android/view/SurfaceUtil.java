package android.view;

import java.lang.reflect.InvocationTargetException;

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
