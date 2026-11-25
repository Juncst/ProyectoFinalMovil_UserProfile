package com.juncst.userprofile.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.juncst.userprofile.R
import com.juncst.userprofile.data.network.RetrofitClient
import com.juncst.userprofile.data.network.TokenManager
import com.juncst.userprofile.data.repository.UserRepository
import com.juncst.userprofile.ui.LocalNetworkPermission
import com.juncst.userprofile.ui.profile.ProfileActivity
import com.juncst.userprofile.ui.register.RegisterActivity

class LoginActivity : AppCompatActivity() {
    private val viewModel: LoginViewModel by viewModels {
        viewModelFactory {
            initializer { LoginViewModel(UserRepository(RetrofitClient.create(applicationContext))) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tokenManager = TokenManager(applicationContext)
        if (!tokenManager.getToken().isNullOrBlank()) {
            openProfile()
            return
        }

        setContentView(R.layout.activity_login)
        val email = findViewById<TextInputEditText>(R.id.inputEmail)
        val password = findViewById<TextInputEditText>(R.id.inputPassword)
        val login = findViewById<MaterialButton>(R.id.btnLogin)
        val register = findViewById<MaterialButton>(R.id.btnOpenRegister)
        val progress = findViewById<ProgressBar>(R.id.progressLogin)
        val message = findViewById<TextView>(R.id.loginMessage)

        val networkPermission = LocalNetworkPermission(this, {
            viewModel.login(email.text.toString(), password.text.toString())
        })
        login.setOnClickListener { networkPermission.run() }
        register.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }

        viewModel.state.observe(this) { state ->
            progress.isVisible = state.loading
            login.isEnabled = !state.loading
            register.isEnabled = !state.loading
            message.text = state.message
            message.isVisible = state.message.isNotEmpty()

            state.token?.let { token ->
                tokenManager.saveToken(token)
                openProfile()
            }
        }
    }

    private fun openProfile() {
        startActivity(Intent(this, ProfileActivity::class.java))
        finish()
    }
}
