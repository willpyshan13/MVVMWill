package com.will.habit.http

/**
 * @author will
 */
class ResponseThrowable(throwable: Throwable?, var code: Int) : Exception(throwable) {
   override var message:String? = null
}