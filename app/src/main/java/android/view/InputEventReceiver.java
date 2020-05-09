package android.view;

import android.os.Looper;
import android.service.wallpaper.WallpaperService;

public abstract class InputEventReceiver {
    public InputEventReceiver(InputChannel inputChannel, Looper looper){

    }
    
    public void onInputEvent(InputEvent event){
    }

    public void finishInputEvent(InputEvent event, boolean handled) {
    }
    class SS extends WallpaperService{

        @Override
        public Engine onCreateEngine() {
            return null;
        }
    }
}
