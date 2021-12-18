package com.spyneai.base.network

import java.io.IOException

class ServerException(val status : Int,
                      private val errorMessage : String,
val response: String?= null) : IOException() {
    override val message: String?
        get() = errorMessage

    override fun hashCode(): Int = status
}