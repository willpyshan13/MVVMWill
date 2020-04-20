package com.will.habit.service

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.will.habit.entity.RespAppPhoneEntity
import com.will.habit.http.BaseResponse
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface CallAdapterApiService {
    @GET("api.php/ActivityData")
    fun getIOSGank(@Query("mobile")number:String): Deferred<RespAppPhoneEntity>
}

class ApiSource {
    companion object {
        @JvmField
        val callAdapterInstance = Retrofit.Builder()
                .baseUrl("http://ac.qgsqw.com/")
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(CallAdapterApiService::class.java)
    }
}