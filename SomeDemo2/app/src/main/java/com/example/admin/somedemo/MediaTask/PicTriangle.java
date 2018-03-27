package com.example.admin.somedemo.MediaTask;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.example.admin.somedemo.util.MatrixState;
import com.example.admin.somedemo.util.ShaderUtil;

import java.lang.reflect.GenericArrayType;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.zip.DeflaterOutputStream;

public class PicTriangle {

    int mProgram;
    int muMVPMatrixHandle;
    int maPositionHandle;
    int maTexCoorHandle;
    String mVertexShader;
    String mFragmentShader;
    FloatBuffer mVertexBuffer;
    FloatBuffer mTexCoorBuffer;
    int vCount = 0;
    float xAngle = 0;
    float yAngle = 0;
    float zAngle = 0;
    static float[] mMMatrix = new float[16];

    public PicTriangle(MyGlPicView mv) {
        intiVertexData();
        initShader(mv);
    }

    public void intiVertexData() {
        vCount = 3;//顶点数量为3
        final float UNIT_SIZE = 0.15f;
        //顶点坐标数组
        float vertices[] = new float[]{
                0 * UNIT_SIZE, 11 * UNIT_SIZE, 0,
                -11 * UNIT_SIZE, -11 * UNIT_SIZE, 0,
                11 * UNIT_SIZE, -11 * UNIT_SIZE, 0,
        };
        //加载进buffer中
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = vbb.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);
        //纹理坐标
        float texCoor[] = new float[]{
                0.5f, 0,
                1, 0,
                1, 1,
        };
        //创建纹理坐标缓冲
        ByteBuffer cbb = ByteBuffer.allocateDirect(texCoor.length * 4);
        cbb.order(ByteOrder.nativeOrder());
        mTexCoorBuffer = cbb.asFloatBuffer();
        mTexCoorBuffer.put(texCoor);
        mTexCoorBuffer.position(0);
    }

    public void initShader(MyGlPicView myGlPicView) {
        //加载顶点着色器的脚本内容
        mVertexShader = ShaderUtil.loadFromAssetsFile("tex_vertex.glsl", myGlPicView.getResources());
        //加载片段着色器的脚本内容
        mFragmentShader = ShaderUtil.loadFromAssetsFile("tex_frag.glsl", myGlPicView.getResources());
        //基于两个shader创建Program
        mProgram = ShaderUtil.createProgram(mVertexShader, mFragmentShader);
        //获取shader当中相关的属性
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        maTexCoorHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoor");
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
    }

    public void drawSelf(int texId) {
        GLES20.glUseProgram(mProgram);
        MatrixState.setInitStack();

        //设置沿Z轴正向位移1
        MatrixState.transtate(0, 0, 1);

        //设置绕y轴旋转
        MatrixState.rotate(yAngle, 0, 1, 0);
        //设置绕z轴旋转
        MatrixState.rotate(zAngle, 0, 0, 1);
        //设置绕x轴旋转
        MatrixState.rotate(xAngle, 1, 0, 0);
        //将最终变换矩阵传入渲染管线
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, MatrixState.getFinalMatrix(), 0);
        //将顶点坐标传入渲染管线
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mVertexBuffer);
        //将纹理坐标传入渲染管线
        GLES20.glVertexAttribPointer(maTexCoorHandle, 2, GLES20.GL_FLOAT, false, 2 * 4, mTexCoorBuffer);
        //启用顶点坐标数据
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        //启用纹理坐标数据
        GLES20.glEnableVertexAttribArray(maTexCoorHandle);
        //激活纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE);
        //绑定加载的图片纹理id
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);

        //绘制纹理三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount);
    }
}
