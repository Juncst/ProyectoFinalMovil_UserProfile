package com.juncst.userprofile.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juncst.userprofile.data.model.User
import com.juncst.userprofile.data.repository.UserRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.io.IOException

data class ProfileState(
    val loading: Boolean = false,
    val user: User? = null,
    val sessionEnded: Boolean = false,
    val message: String = ""
)

class ProfileViewModel(private val repository: UserRepository) : ViewModel() {
    private val stateData = MutableLiveData(ProfileState())
    val state: LiveData<ProfileState> = stateData

    fun showPermissionError() {
        stateData.value = ProfileState(message = "Permite el acceso a la red local para cargar el perfil.")
    }

    fun loadProfile() {
        if (stateData.value?.loading == true) return

        viewModelScope.launch {
            stateData.value = ProfileState(loading = true)
            stateData.value = try {
                val response = repository.getProfile()
                val user = response.body()
                when {
                    response.isSuccessful && user != null -> ProfileState(user = user)
                    response.code() == 401 || response.code() == 404 -> ProfileState(sessionEnded = true)
                    else -> ProfileState(message = "No se pudo cargar el perfil")
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: IOException) {
                ProfileState(message = "No se pudo conectar con el servidor")
            } catch (error: Exception) {
                ProfileState(message = "El servidor devolvió una respuesta inesperada")
            }
        }
    }
}
