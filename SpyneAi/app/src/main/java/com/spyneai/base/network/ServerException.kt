package com.spyneai.base.network

import java.io.IOException
import java.lang.Exception

class ServerException(val status : Int, private val errorMessage : String) : IOException() {
    override val message: String?
        get() = errorMessage

    override fun hashCode(): Int = status
}