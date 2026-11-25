package com.juncst.userprofile.data.model

data class LoginResponse(
    val message: String,
    val token: String,
    val user: User
)
