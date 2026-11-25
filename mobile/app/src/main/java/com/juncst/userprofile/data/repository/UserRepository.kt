package com.juncst.userprofile.data.repository

import com.juncst.userprofile.data.model.*
import com.juncst.userprofile.data.network.ApiService
import retrofit2.Response

class UserRepository(
    private val apiService: ApiService
) {

    suspend fun register(
        request: RegisterRequest
    ): Response<RegisterResponse> {
        return apiService.register(request)
    }

    suspend fun login(
        request: LoginRequest
    ): Response<LoginResponse> {
        return apiService.login(request)
    }

    suspend fun getProfile(): Response<User> {
        return apiService.getProfile()
    }
}
