package com.juncst.userprofile.ui.profile

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.material.button.MaterialButton
import com.juncst.userprofile.R
import com.juncst.userprofile.data.network.RetrofitClient
import com.juncst.userprofile.data.network.TokenManager
import com.juncst.userprofile.data.repository.UserRepository
import com.juncst.userprofile.ui.LocalNetworkPermission
import com.juncst.userprofile.ui.login.LoginActivity
import java.io.File
import kotlin.math.ceil

class ProfileActivity : AppCompatActivity() {
    private val viewModel: ProfileViewModel by viewModels {
        viewModelFactory {
            initializer { ProfileViewModel(UserRepository(RetrofitClient.create(applicationContext))) }
        }
    }
    private lateinit var image: ImageView
    private var photoUri: Uri? = null
    private var cameraFile: File? = null
    private var displayedFile: File? = null

    private val cameraContract = object : ActivityResultContracts.TakePicture() {
        override fun createIntent(context: Context, input: Uri): Intent =
            super.createIntent(context, input).apply {
                clipData = ClipData.newRawUri("Fotografía de perfil", input)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
    }

    private val takePicture = registerForActivityResult(cameraContract) { saved ->
        val file = cameraFile
        cameraFile = null
        if (file != null) {
            if (saved && showPhoto(cameraUri(file))) {
                displayedFile?.delete()
                displayedFile = file
            } else {
                file.delete()
            }
        }
    }

    private val pickPhoto = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null && showPhoto(uri)) {
            displayedFile?.delete()
            displayedFile = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (TokenManager(applicationContext).getToken().isNullOrBlank()) {
            returnToLogin()
            return
        }

        setContentView(R.layout.activity_profile)
        image = findViewById(R.id.profileImage)
        val name = findViewById<TextView>(R.id.profileName)
        val email = findViewById<TextView>(R.id.profileEmail)
        val message = findViewById<TextView>(R.id.profileMessage)
        val progress = findViewById<ProgressBar>(R.id.progressProfile)
        val retry = findViewById<MaterialButton>(R.id.btnRetryProfile)
        val camera = findViewById<MaterialButton>(R.id.btnCamera)
        val gallery = findViewById<MaterialButton>(R.id.btnGallery)

        cameraFile = savedInstanceState?.getString("cameraFile")?.let(::File)
        displayedFile = savedInstanceState?.getString("displayedFile")?.let(::File)
        savedInstanceState?.getString("photoUri")?.toUri()?.let { showPhoto(it) }

        camera.setOnClickListener { openCamera() }
        gallery.setOnClickListener {
            pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        val networkPermission = LocalNetworkPermission(this, viewModel::loadProfile, viewModel::showPermissionError)
        retry.setOnClickListener { networkPermission.run() }
        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener { returnToLogin() }

        viewModel.state.observe(this) { state ->
            if (state.sessionEnded) {
                notifyUser("La sesión terminó. Inicia sesión de nuevo.")
                returnToLogin()
                return@observe
            }

            val user = state.user
            progress.isVisible = state.loading
            name.text = user?.full_name
            email.text = user?.email
            name.isVisible = user != null
            email.isVisible = user != null
            image.isVisible = user != null
            camera.isVisible = user != null
            gallery.isVisible = user != null
            message.text = state.message
            message.isVisible = state.message.isNotEmpty()
            retry.isVisible = state.message.isNotEmpty()
        }
        if (savedInstanceState == null || viewModel.state.value == ProfileState()) {
            networkPermission.run()
        }
    }

    private fun openCamera() {
        if (cameraFile != null) return
        try {
            val directory = File(cacheDir, "profile_photos").apply { mkdirs() }
            val file = File.createTempFile("profile_", ".jpg", directory)
            cameraFile = file
            takePicture.launch(cameraUri(file))
        } catch (error: Exception) {
            cameraFile?.delete()
            cameraFile = null
            notifyUser("No se pudo abrir la cámara")
        }
    }

    private fun cameraUri(file: File): Uri =
        FileProvider.getUriForFile(this, "$packageName.profilephotos", file)

    private fun showPhoto(uri: Uri): Boolean {
        return try {
            val bitmap = if (Build.VERSION.SDK_INT >= 28) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri)) { decoder, info, _ ->
                    val maxSide = maxOf(info.size.width, info.size.height)
                    decoder.setTargetSampleSize(ceil(maxSide / 768.0).toInt().coerceAtLeast(1))
                }
            } else {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                val options = BitmapFactory.Options()
                val maxSide = maxOf(bounds.outWidth, bounds.outHeight)
                while (maxSide / options.inSampleSize.coerceAtLeast(1) > 768) {
                    options.inSampleSize = options.inSampleSize.coerceAtLeast(1) * 2
                }
                contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            }

            if (bitmap == null) {
                notifyUser("No se pudo mostrar la fotografía")
                return false
            }
            image.setImageBitmap(bitmap)
            photoUri = uri
            true
        } catch (error: Exception) {
            notifyUser("No se pudo mostrar la fotografía")
            false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("photoUri", photoUri?.toString())
        outState.putString("cameraFile", cameraFile?.path)
        outState.putString("displayedFile", displayedFile?.path)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        if (isFinishing) {
            cameraFile?.delete()
            displayedFile?.delete()
        }
        super.onDestroy()
    }

    private fun returnToLogin() {
        TokenManager(applicationContext).clearToken()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun notifyUser(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
