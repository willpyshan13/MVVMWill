package com.will.habit.repository

import com.will.habit.base.BaseModel
import com.will.habit.entity.RespAppPhoneEntity
import com.will.habit.http.BaseResponse
import com.will.habit.service.ApiSource
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Collections.addAll

class PhoneRepository : BaseModel() {
    /**
     * 两个请求在子线程中顺序执行，非同时并发
     */
    suspend fun querySyncWithContext(phoneNumber:String,imei:String): RespAppPhoneEntity {
        return withContext(Dispatchers.Main) {
            try {
                val androidResult = ApiSource.callAdapterInstance.getIOSGank(phoneNumber,imei)
                androidResult.await()
            } catch (e: Throwable) {
                e.printStackTrace()
                throw e
            }
        }
    }
}