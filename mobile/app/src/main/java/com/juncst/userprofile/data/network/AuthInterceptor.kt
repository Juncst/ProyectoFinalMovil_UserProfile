package com.juncst.userprofile.data.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()
        val builder = request.newBuilder()

        if (request.url.encodedPath == "/api/profile") {
            val token = tokenManager.getToken()

            if (!token.isNullOrBlank()) {
                builder.header(
                    "Authorization",
                    "Bearer $token"
                )
            }
        }

        return chain.proceed(builder.build())
    }
}
