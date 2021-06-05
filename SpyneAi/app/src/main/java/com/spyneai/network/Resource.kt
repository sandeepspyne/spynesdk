package com.spyneai.dashboard.network

import okhttp3.ResponseBody

sealed class Resource<out T> {
    data class Sucess<out T>(val value: T) : Resource<T>()
    data class Failure(
        val isNetworkError: Boolean,
        val errorCode: Int?,
        val errorBody: ResponseBody?
    ) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}