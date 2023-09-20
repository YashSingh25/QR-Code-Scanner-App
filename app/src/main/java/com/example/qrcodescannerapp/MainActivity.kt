package com.example.qrcodescannerapp

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CompoundBarcodeView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var barcodeView: CompoundBarcodeView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        barcodeView = findViewById(R.id.barcodeView)

        if (allPermissionsGranted()) {
            startScanning()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED


    private fun startScanning() {
        barcodeView.resume()
        barcodeView.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                result?.let {

                    GlobalScope.launch(Dispatchers.Main) {
                        val scannedText = result.text
                        processScannedTextInBackground(scannedText)
                    }
                }
            }

            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {}
        })

        findViewById<Button>(R.id.scanButton).setOnClickListener {
            // Start scanning when the button is clicked
            barcodeView.resume()
        }
    }

    private fun processScannedTextInBackground(scannedText: String) {
        Log.e("yo", "$scannedText" )
        Toast.makeText(applicationContext,"$scannedText", Toast.LENGTH_LONG).show()
        handleScannedQRCode(scannedText)
    }

    private fun handleScannedQRCode(qrCodeContent: String) {
        val intent = Intent(Intent.ACTION_VIEW)

        //
        when {

            (qrCodeContent.startsWith("http://") || qrCodeContent.startsWith("https://")) -> {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(qrCodeContent))
                startActivity(browserIntent)
            }


            qrCodeContent.startsWith("paytm://") -> {
                // Payment request for Paytm
                intent.data = Uri.parse(qrCodeContent)
                openAppOrPromptToInstall(intent, "com.paytm.android")
            }
            qrCodeContent.startsWith("upi://") -> {
                // Payment request using UPI (e.g., Google Pay)
                intent.data = Uri.parse(qrCodeContent)
                openAppOrPromptToInstall(intent, "com.google.android.apps.nbu.paisa.user")
            }
            // Add more conditions for other payment apps
            // Example: PhonePe
            qrCodeContent.startsWith("phonepe://") -> {
                intent.data = Uri.parse(qrCodeContent)
                openAppOrPromptToInstall(intent, "com.phonepe.app")
            }

            else -> {

            }
        }
    }

    private fun openAppOrPromptToInstall(intent: Intent, packageName: String) {
        try {
            // Check if the app is installed
            val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            if (packageInfo != null) {

                intent.setPackage(packageName)
                startActivity(intent)
            } else {

                val playStoreIntent = Intent(Intent.ACTION_VIEW)
                playStoreIntent.data = Uri.parse("market://details?id=$packageName")
                startActivity(playStoreIntent)
            }
        } catch (e: PackageManager.NameNotFoundException) {

        } catch (e: ActivityNotFoundException) {

        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startScanning()
            } else {

            }
        }
    }


    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }


    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

}