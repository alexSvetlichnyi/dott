package com.example.dott.data.network

import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response = chain.run {
        proceed(
            request()
                .newBuilder()
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "fsq3c3EtlPTk7NzNN05sUyjdtxADuwj1mNUhhW5bi7jsROQ=")
                .build()
        )        
    }
}