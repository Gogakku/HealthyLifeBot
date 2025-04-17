package org.example.org.example

data class UserProfile(
    var name: String = "",
    var age: Int? = null,
    var height: Int? = null,
    var weight: Int? = null,
    var goal: String = "",
    var lives: Int = 3
)
