package com.will.mvvm.http

/**
 *
 */
class BaseResponse<T> {
    @JvmField
    var code = 0
    @JvmField
    var message: String? = null

    var result: T? = null
        private set

    fun setResult(result: T) {
        this.result = result
    }

    val isOk: Boolean
        get() = code == 0

}