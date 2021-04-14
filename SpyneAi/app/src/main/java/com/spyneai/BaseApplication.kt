package com.spyneai

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

@SuppressLint("StaticFieldLeak")
class BaseApplication : Application() {

    companion object {

        private lateinit var context: Context;

        public fun getContext(): Context {
            return context;
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = this
    }
}