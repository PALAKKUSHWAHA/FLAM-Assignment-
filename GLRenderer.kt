package com.example.edgedetectionviewer

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLRenderer : GLSurfaceView.Renderer {
    private lateinit var textureView: TextureView
    private var textureId = -1
    private var surfaceTexture: SurfaceTexture? = null
    
    fun setTextureView(textureView: TextureView) {
        this.textureView = textureView
    }
    
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        textureId = createTexture()
        surfaceTexture = SurfaceTexture(textureId)
        textureView.surfaceTexture = surfaceTexture
        
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
    }
    
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }
    
    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        surfaceTexture?.updateTexImage()
        
        // Here we would process the frame with OpenCV via JNI
        // For now, just display the raw camera feed
    }
    
    private fun createTexture(): Int {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0])
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
        return textures[0]
    }
    
    companion object {
        init {
            System.loadLibrary("edge-detection")
        }
        
        external fun processFrame(texIn: Int, width: Int, height: Int): Int
    }
}