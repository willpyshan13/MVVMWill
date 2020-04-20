package com.will.habit.extection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*

/**
 * 拓展ViewModel，统一viewModelScope调用方式
 * @param block() 挂起函数
 * @param fail() 请求失败回调，可选参数，参数t为[ResponseException]时表示接口异常
 * @param toastNetWorkError 网络异常是否toast，可选参数，默认true
 * @param toastResponseError 接口返回code!=10000时是否toast，可选参数，默认true
 *
 * 栗子：
 * launch({
 *      // api请求
 * },{
 *      // t is ResponseException -> code!=10000
 * })
 */
fun ViewModel.launch(block: suspend (coroutineScope: CoroutineScope) -> Unit,
                     fail: (t: Throwable) -> Unit = { },
                     toastNetWorkError: Boolean = true,
                     toastResponseError: Boolean = true) =
        viewModelScope.safeLaunch(block, fail, toastNetWorkError, toastResponseError)

fun CoroutineScope.safeLaunch(block: suspend (coroutineScope: CoroutineScope) -> Unit,
                              fail: (t: Throwable) -> Unit = { },
                              toastNetWorkError: Boolean = true,
                              toastResponseError: Boolean = true) = launch {
    try {
        block(this)
    } catch (t: Throwable) {
        t.printStackTrace()
        fail(t)
    }
}
