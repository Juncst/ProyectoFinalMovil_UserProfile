package com.juncst.userprofile.ui.register

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juncst.userprofile.data.model.RegisterRequest
import com.juncst.userprofile.data.repository.UserRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException

data class RegisterState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String = ""
)

class RegisterViewModel(
    private val repository: UserRepository
) : ViewModel() {

    private val stateData = MutableLiveData(RegisterState())
    val state: LiveData<RegisterState> = stateData

    fun register(fullName: String, email: String, password: String) {
        if (stateData.value?.loading == true || stateData.value?.success == true) return

        val name = fullName.trim()
        val correo = email.trim()

        if (name.isEmpty() || correo.isEmpty() || password.isEmpty()) {
            stateData.value = RegisterState(message = "Completa todos los campos")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            stateData.value = RegisterState(message = "Correo electrónico inválido")
            return
        }

        if (password.codePointCount(0, password.length) < 8) {
            stateData.value = RegisterState(message = "La contraseña debe tener al menos 8 caracteres")
            return
        }

        if (password.toByteArray(Charsets.UTF_8).size > 72) {
            stateData.value = RegisterState(message = "La contraseña admite hasta 72 bytes")
            return
        }

        viewModelScope.launch {
            stateData.value = RegisterState(loading = true)

            try {
                val response = repository.register(
                    RegisterRequest(name, correo, password)
                )

                if (response.isSuccessful) {
                    stateData.value = RegisterState(
                        success = true,
                        message = "Usuario registrado correctamente"
                    )
                } else {
                    val error = response.errorBody()?.string().orEmpty()
                    val message = runCatching {
                        JSONObject(error).optString("message")
                    }.getOrDefault("")

                    stateData.value = RegisterState(
                        message = message.ifBlank { "No se pudo registrar el usuario" }
                    )
                }

            } catch (error: CancellationException) {
                throw error
            } catch (error: IOException) {
                stateData.value = RegisterState(
                    message = "Error de conexión con el servidor"
                )
            } catch (error: Exception) {
                stateData.value = RegisterState(message = "El servidor devolvió una respuesta inesperada")
            }
        }
    }
}
