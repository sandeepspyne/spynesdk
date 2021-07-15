package com.spyneai.base

import com.spyneai.base.network.Resource
import com.spyneai.base.network.ServerException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

abstract class BaseRepository {

    suspend fun <T> safeApiCall(
        apiCall: suspend () -> T
    ) : Resource<T> {
        return withContext(Dispatchers.IO){
            try {
                Resource.Success(apiCall.invoke())
            }catch (throwable: Throwable){
                val s = ""
                when(throwable){

                    is ServerException -> {
                        Resource.Failure(false, throwable.hashCode(), throwable.message)
                    }

                    is HttpException -> {
                        Resource.Failure(false, throwable.code(), throwable.response()?.errorBody().toString())
                    }

                    else -> {
                        Resource.Failure(true, null, "Please check your internet connection")
                    }
                }
            }
        }
    }
}