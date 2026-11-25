package com.juncst.userprofile.data.model

data class RegisterRequest(
    val full_name: String,
    val email: String,
    val password: String
)