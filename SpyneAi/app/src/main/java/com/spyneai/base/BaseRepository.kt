package com.spyneai.base

import com.google.gson.JsonSyntaxException
import com.spyneai.base.network.Resource
import com.spyneai.base.network.ServerException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLHandshakeException

abstract class BaseRepository {

    suspend fun <T> safeApiCall(
        apiCall: suspend () -> T
    ) : Resource<T> {
        return withContext(Dispatchers.IO){
            try {
                Resource.Success(apiCall.invoke())
            }catch (throwable: Throwable){
                when(throwable){

                    is ServerException -> {
                        Resource.Failure(false, throwable.hashCode(), throwable.message)
                    }

                    is HttpException -> {
                        Resource.Failure(false, throwable.code(), throwable.response()?.errorBody().toString())
                    }

                    is JsonSyntaxException -> {
                        Resource.Failure(false, throwable.hashCode(), throwable.message)
                    }

                    is SocketTimeoutException -> {
                        Resource.Failure(false, throwable.hashCode(), throwable.message)
                    }

                    is SSLHandshakeException -> {
                        Resource.Failure(false, throwable.hashCode(), throwable.message)
                    }

                    else -> {
                        Resource.Failure(true, null, "Please check your internet connection")
                    }
                }
            }
        }
    }
}