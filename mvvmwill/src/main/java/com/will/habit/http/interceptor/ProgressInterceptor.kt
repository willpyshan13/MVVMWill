package com.will.habit.http.interceptor

import com.will.habit.http.download.ProgressResponseBody
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 *  @author will
 */
class ProgressInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())
        return originalResponse.newBuilder()
                .body(ProgressResponseBody(originalResponse.body()))
                .build()
    }
}