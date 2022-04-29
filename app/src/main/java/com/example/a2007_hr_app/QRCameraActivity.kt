/**
 * CameraX analysis + ML kit barcode scanner to read QR codes
 * Author: WongJunHao
 * References: CodeLabs, Android, ML Kit documentation
 * Ref-Link: https://developer.android.com/codelabs/camerax-getting-started#2
 * Ref-Link: https://developers.google.com/ml-kit/vision/barcode-scanning/android
 */
package com.example.a2007_hr_app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.a2007_hr_app.databinding.ActivityCameraBinding
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class QRCameraActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityCameraBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        supportActionBar?.hide()

        /**
         * Request camera permissions if not granted.
         */
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        /**
         * Setup camera executor on new thread
         */
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    /**
     * Kill cameraExecutor thread
     */
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        //Bind the lifecycle of cameras to the lifecycle owner
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, QrAnalyzer(this))
                }

            //Select the back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer
                )

            } catch (exc: Exception) {
                Log.e("CameraActivity", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private class QrAnalyzer(activity: QRCameraActivity) : ImageAnalysis.Analyzer {

        private val activity = activity
        private val sharedPref = this.activity.getSharedPreferences(
            "com.example.a2007_hr_app.PREFERENCE_FILE_KEY",
            Context.MODE_PRIVATE
        )


        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        override fun analyze(imageProxy: ImageProxy) {

            //Set-up BarcodeScanner Options
            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_QR_CODE
                )
                .build()

            @androidx.camera.core.ExperimentalGetImage
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image =
                    InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                // Pass image to an ML Kit Vision API
                val scanner = BarcodeScanning.getClient()
                val result = scanner.process(image)
                    .addOnSuccessListener { barcodes ->

                        //Get barcode information
                        for (barcode in barcodes) {

                            // See API reference for complete list of supported types
                            when (barcode.valueType) {
                                Barcode.TYPE_TEXT -> {
                                    val text = barcode.displayValue
                                    Log.d("barcode", "$text")

                                    /**
                                     * If location is correct save check-in and finish activity
                                     * Note: Ideally the QR Text Location should be checked against a list
                                     */
                                    if (text == "SIT@NYP") {
                                        with(sharedPref.edit()) {
                                            putString("QR_Location", text)
                                            apply()
                                        }
                                        Log.d(
                                            "barcode_sharedPref",
                                            "${sharedPref.getString("QR_Location", "None")}"
                                        )
                                        activity.finish()
                                    }
                                }
                            }
                        }
                        imageProxy.close()
                    }
                    .addOnFailureListener {
                        Log.d("Barcode", "Failed")
                    }
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}