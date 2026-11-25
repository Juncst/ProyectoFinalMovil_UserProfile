package com.juncst.userprofile.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juncst.userprofile.data.model.LoginRequest
import com.juncst.userprofile.data.repository.UserRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.io.IOException

data class LoginState(
    val loading: Boolean = false,
    val token: String? = null,
    val message: String = ""
)

class LoginViewModel(private val repository: UserRepository) : ViewModel() {
    private val stateData = MutableLiveData(LoginState())
    val state: LiveData<LoginState> = stateData

    fun login(email: String, password: String) {
        if (stateData.value?.loading == true) return

        val address = email.trim()
        if (address.isEmpty() || password.isEmpty()) {
            stateData.value = LoginState(message = "Ingresa tu correo y contraseña")
            return
        }

        viewModelScope.launch {
            stateData.value = LoginState(loading = true)
            stateData.value = try {
                val response = repository.login(LoginRequest(address, password))
                val token = response.body()?.token
                when {
                    response.isSuccessful && !token.isNullOrBlank() -> LoginState(token = token)
                    response.code() == 401 -> LoginState(message = "Correo o contraseña incorrectos")
                    else -> LoginState(message = "No se pudo iniciar sesión. Intenta nuevamente.")
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: IOException) {
                LoginState(message = "No se pudo conectar con el servidor")
            } catch (error: Exception) {
                LoginState(message = "El servidor devolvió una respuesta inesperada")
            }
        }
    }
}
