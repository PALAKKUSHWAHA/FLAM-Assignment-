package com.example.edgedetectionviewer

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class CameraActivity : AppCompatActivity() {
    private lateinit var textureView: TextureView
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraDevice: CameraDevice
    private lateinit var handler: Handler
    private lateinit var handlerThread: HandlerThread
    private lateinit var renderer: GLRenderer
    
    private val cameraCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            createCameraPreview()
        }
        
        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
        }
        
        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
        }
    }
    
    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            openCamera()
        }
        
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = false
        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        
        textureView = findViewById(R.id.texture_view)
        textureView.surfaceTextureListener = surfaceTextureListener
        
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        handlerThread = HandlerThread("CameraBackground")
        handlerThread.start()
        handler = Handler(handlerThread.looper)
        
        renderer = GLRenderer()
        renderer.setTextureView(textureView)
    }
    
    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != 
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
            return
        }
        
        val cameraId = cameraManager.cameraIdList[0]
        cameraManager.openCamera(cameraId, cameraCallback, handler)
    }
    
    private fun createCameraPreview() {
        val surfaceTexture = textureView.surfaceTexture
        val previewSize = getPreviewSize()
        surfaceTexture.setDefaultBufferSize(previewSize.width, previewSize.height)
        val surface = Surface(surfaceTexture)
        
        val captureRequest = cameraDevice.createCaptureRequest(
            CameraDevice.TEMPLATE_PREVIEW
        ).apply {
            addTarget(surface)
        }
        
        cameraDevice.createCaptureSession(
            listOf(surface),
            object : CameraDevice.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    session.setRepeatingRequest(captureRequest.build(), null, handler)
                }
                
                override fun onConfigureFailed(session: CameraCaptureSession) {}
            },
            handler
        )
    }
    
    private fun getPreviewSize(): Size {
        val characteristics = cameraManager.getCameraCharacteristics(cameraManager.cameraIdList[0])
        val streamConfigs = characteristics.get(
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
        )
        return streamConfigs?.getOutputSizes(SurfaceTexture::class.java)?.get(0) ?: Size(640, 480)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cameraDevice.close()
        handlerThread.quitSafely()
    }
}