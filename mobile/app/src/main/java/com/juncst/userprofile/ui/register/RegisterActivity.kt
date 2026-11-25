package com.juncst.userprofile.ui.register

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.juncst.userprofile.R
import com.juncst.userprofile.data.network.RetrofitClient
import com.juncst.userprofile.data.repository.UserRepository
import com.juncst.userprofile.ui.LocalNetworkPermission

class RegisterActivity : AppCompatActivity() {

    private lateinit var viewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val inputName = findViewById<TextInputEditText>(R.id.inputFullName)
        val inputEmail = findViewById<TextInputEditText>(R.id.inputEmail)
        val inputPassword = findViewById<TextInputEditText>(R.id.inputPassword)
        val btnRegister = findViewById<MaterialButton>(R.id.btnRegister)
        val progress = findViewById<ProgressBar>(R.id.progressRegister)
        val message = findViewById<TextView>(R.id.registerMessage)
        findViewById<MaterialButton>(R.id.btnBackToLogin).setOnClickListener { finish() }

        val apiService = RetrofitClient.create(applicationContext)
        val repository = UserRepository(apiService)

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return RegisterViewModel(repository) as T
                }

                throw IllegalArgumentException("ViewModel desconocido")
            }
        }

        viewModel = ViewModelProvider(
            this,
            factory
        )[RegisterViewModel::class.java]

        val networkPermission = LocalNetworkPermission(this, {
            val name = inputName.text.toString()
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()

            viewModel.register(name, email, password)
        })
        btnRegister.setOnClickListener { networkPermission.run() }

        viewModel.state.observe(this) { state ->

            progress.visibility = if (state.loading) {
                View.VISIBLE
            } else {
                View.GONE
            }

            btnRegister.isEnabled = !state.loading && !state.success
            message.text = state.message
            message.isVisible = !state.success && state.message.isNotEmpty()

            if (state.success) {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Registro exitoso")
                    .setMessage("Tu cuenta fue creada correctamente. Ya puedes iniciar sesión.")
                    .setCancelable(false)
                    .setPositiveButton("Ir al inicio de sesión") { _, _ -> finish() }
                    .show()
            }
        }
    }
}
