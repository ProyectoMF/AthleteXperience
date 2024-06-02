package com.example.athletexperience


data class Exercise(
    val name: String = "",
    val sets: MutableList<Set> = mutableListOf()
)