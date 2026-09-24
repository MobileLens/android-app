package com.mobilelens.mobilelens.auth.model

data class User(
    val id: String = "",
    val username: String = "username",
    val email: String = "user@example.com",
    val role: String = "Reviewer"
)
