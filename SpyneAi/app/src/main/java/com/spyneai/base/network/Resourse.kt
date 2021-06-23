package com.spyneai.base.network

import okhttp3.ResponseBody

sealed class Resource<out T> {
    data class Sucess<out T>(val value: T) : Resource<T>()
    data class Failure(
        val isNetworkError: Boolean,
        val errorCode: Int?,
        val errorMessage: String?
    ) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}