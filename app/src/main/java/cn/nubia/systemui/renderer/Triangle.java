package cn.nubia.systemui.renderer;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Triangle {
    public static float[] mProjMatrix = new float[16]; //4*4
    public static float[] mVMatrix = new float[16];
    public static float[] mMVPMatrix;

    int mProgram;
    int muMVPMatrixHandle;
    int maPosistionHandle;
    int maColorHandle;
    final String mVertexShder =
                "#version 300 es;\n" +
                "uniform mat4 uMVPMatrix;\n" +
                "layout (location = 0) in vec3 aPosition;\n" +
                "layout (location = 1) in vec4 aColor;\n" +
                "out vec4 vColor;\n" +
                "void main(){\n" +
                "    gl_Position = uMVPMatrix * vec4(aPosition, 1);\n" +
                "    vColor = aColor" +
                "}";
    String mFragmentShader =
                "#version 300 es\n" +
                "precision mediump float;\n" +
                "in vec4 vColor;\n" +
                "out vec4 fragColor;" +
                "void main(){\n" +
                "    fragColor = vColor;\n" +
                "}";
    static float[] mMMatrix = new float[16];
    FloatBuffer mVertexBuffer;
    FloatBuffer mColorBuffer;
    public int vCount = 0;
    public float xAngle = 0;

    public Triangle(GLSurfaceView suerfaceView) {
        initVertexData();
        initShader();
    }

    public void initVertexData(){
        vCount = 3;
        final float UNIT_SIZE = 0.2F;
        float[] vertices = new float[]{
            -4*UNIT_SIZE, 0, 0, 0, -4*UNIT_SIZE, 0, 4*UNIT_SIZE, 0, 0
        };
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length*4);
        vbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = vbb.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);
        float[] colors = new float[]{
                1, 1, 1, 0, 0, 0, 1, 0,0 ,1, 0, 0
        };
        ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length*4);
        cbb.order(ByteOrder.nativeOrder());
        mColorBuffer = cbb.asFloatBuffer();
        mColorBuffer.put(colors);
        mColorBuffer.position(0);
    }

    public void initShader(){
        mProgram = ShaderUtil.createProgram(mVertexShder, mFragmentShader);

        maPosistionHandle = GLES30.glGetAttribLocation(mProgram, "aPosition");
        maColorHandle = GLES30.glGetAttribLocation(mProgram, "aColor");
        muMVPMatrixHandle = GLES30.glGetUniformLocation(mProgram, "uMVPMatrix");
    }

    public static float[] getFinalMatrix(float[] spec){
        mMVPMatrix = new float[16];
        Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, spec, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix,  0, mMVPMatrix, 0);
        return mMVPMatrix;
    }

    public void drawSelf(){
        GLES30.glUseProgram(mProgram);
        Matrix.setRotateM(mMMatrix, 0 , 0, 0, 1, 0);
        Matrix.translateM(mMMatrix, 0, 0, 0, 1);
        Matrix.rotateM(mMMatrix, 0, xAngle, 1, 0, 0);
        GLES30.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, getFinalMatrix(mMMatrix), 0);
        GLES30.glVertexAttribPointer(maPosistionHandle, 3, GLES30.GL_FLOAT, false, 3*4, mVertexBuffer);
        GLES30.glVertexAttribPointer(maColorHandle, 4, GLES30.GL_FLOAT, false, 4*4, mColorBuffer);

        GLES30.glEnableVertexAttribArray(maPosistionHandle);
        GLES30.glEnableVertexAttribArray(maColorHandle);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vCount);
    }
}
