package com.example.dott.data.network

/**
 * Resource wrap data. Can return success with data or error with message.
 */
sealed class Resource<out T: Any> {
    data class Success<out T: Any>(val data: T): Resource<T>()
    data class Error(val errorMessage: String): Resource<Nothing>()
}
