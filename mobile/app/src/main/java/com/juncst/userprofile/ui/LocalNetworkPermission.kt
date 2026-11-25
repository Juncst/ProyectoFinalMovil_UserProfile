package com.juncst.userprofile.ui

import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

internal class LocalNetworkPermission(
    private val activity: AppCompatActivity,
    private val onGranted: () -> Unit,
    private val onDenied: () -> Unit = {
        Toast.makeText(activity, "Permite el acceso a la red local para conectar con el servidor.", Toast.LENGTH_LONG).show()
    }
) {
    private val permission = "android.permission.ACCESS_LOCAL_NETWORK"
    private val launcher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) onGranted() else onDenied()
    }

    fun run() {
        if (Build.VERSION.SDK_INT < 37 ||
            ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
        ) {
            onGranted()
        } else {
            launcher.launch(permission)
        }
    }
}
