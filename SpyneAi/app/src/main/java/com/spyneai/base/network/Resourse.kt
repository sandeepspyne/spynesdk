package com.spyneai.base.network

sealed class Resource<out T> {
    data class Success<out T>(val value: T) : Resource<T>()
    data class Failure(
        val isNetworkError: Boolean,
        val errorCode: Int?,
        val errorMessage: String?,
        val throwable: String? = null
    ) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}