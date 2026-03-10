package com.scanx.app

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.objects.DetectedObject
import com.scanx.app.data.KnownItem
import com.scanx.app.ui.GraphicOverlay
import com.scanx.app.ui.ProductGraphic
import com.scanx.app.vision.ProductAnalyzer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var graphicOverlay: GraphicOverlay
    private lateinit var btnReset: Button
    private lateinit var cameraExecutor: ExecutorService

    private val knownItems = mutableListOf<KnownItem>()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission required.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.previewView)
        graphicOverlay = findViewById(R.id.graphicOverlay)
        btnReset = findViewById(R.id.btnReset)
        cameraExecutor = Executors.newSingleThreadExecutor()

        btnReset.setOnClickListener {
            knownItems.clear()
            graphicOverlay.clear()
            it.performHapticFeedback(HapticFeedbackConstants.REJECT)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, ProductAnalyzer { detectedObjects, width, height ->
                        graphicOverlay.setImageSourceInfo(height, width)
                        processDetections(detectedObjects)
                    })
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e("ScanX", "Camera binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun processDetections(detectedObjects: List<DetectedObject>) {
        graphicOverlay.clear()

        for (obj in detectedObjects) {
            val currentId = obj.trackingId ?: continue
            val currentRect = obj.boundingBox

            // 1. Check if we already have this exact ID
            var matchedItem = knownItems.find { it.trackingId == currentId }

            // 2. Fallback - If ID changed due to tracking loss or check for overlap
            if (matchedItem == null) {
                matchedItem = knownItems.find { Rect.intersects(it.boundingBox, currentRect) }
                if (matchedItem != null) {
                    matchedItem.trackingId = currentId // Updating to the new ML Kit ID
                }
            }

            if (matchedItem != null) {
                // Update position of existing item
                matchedItem.boundingBox = currentRect
            } else {
                // Register completely new item
                matchedItem = KnownItem(currentId, currentRect)
                knownItems.add(matchedItem)

                // Trigger subtle haptic feedback for UX
                window.decorView.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            }

            graphicOverlay.add(ProductGraphic(graphicOverlay, currentRect, matchedItem.trackingId))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}