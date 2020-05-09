package android.view;

import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.support.annotation.IntDef;

import java.lang.annotation.*;

public class SurfaceControl {

    public static final int HIDDEN = 2<<2;
    public static void openTransaction(){}
    public static void closeTransaction(){}
    public static class Transaction{
        public Transaction remove(SurfaceControl sc){return this;}
        public void applay(){}
    }
    public void setLayer(int zorder){}
    public void setPosition(float x, float y){}
    public void setBufferSize(int width, int height){}
    public void setMatrix(float dsdx, float dtdx, float dtdy, float dsdy){}
    public void setWindowCrop(Rect crop){}
    public void setWindowCrop(int width, int height){}
    public boolean isValid(){return false;}
    public void setAlpha(float alpha){}
    public void show(){}
    public void hide(){}
    public static class Builder{
        public Builder(SurfaceSession session){
        }
        public Builder setName(String name){
            return this;
        }
        public Builder setOpaque(boolean opaque){
            return this;
        }
        public Builder setBufferSize(int width, int height){
            return this;
        }
        public Builder setFormat(int format){
            return this;
        }
        public Builder setProtected(boolean protectedContent){
            return this;
        }
        public Builder setSecure(boolean secure){
            return this;
        }
        public Builder setQpaque(boolean opaque){
            return this;
        }
        public Builder setParent(SurfaceControl parent){
            return this;
        }
        public Builder setMetadata(int key, int data){
            return this;
        }
        public Builder setColorLayer(){
            return this;
        }
        public Builder setContainerLayer(){
            return this;
        }
        public Builder setFlags(int flags){
            return this;
        }
        public Builder setFlags(int flags, int mask){
            return this;
        }
        public boolean isColorLayer(){
            return false;
        }
        public void unsetBufferSize(){}
        public SurfaceControl build(){return null;}
    }
}
