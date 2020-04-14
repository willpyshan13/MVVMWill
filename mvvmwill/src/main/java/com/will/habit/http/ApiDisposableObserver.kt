package com.will.habit.http

import com.will.habit.base.AppManager.Companion.appManager
import com.will.habit.http.NetworkUtil.isNetworkAvailable
import com.will.habit.utils.KLog.d
import com.will.habit.utils.KLog.e
import com.will.habit.utils.ToastUtils
import com.will.habit.utils.Utils
import io.reactivex.observers.DisposableObserver

/**
 * Created by goldze on 2017/5/10.
 * 统一的Code封装处理。该类仅供参考，实际业务逻辑, 根据需求来定义，
 */
abstract class ApiDisposableObserver<T> : DisposableObserver<T>() {
    abstract fun onResult(t: T?)
    override fun onComplete() {}
    override fun onError(e: Throwable) {
        e.printStackTrace()
        if (e is ResponseThrowable) {
            ToastUtils.showShort(e.message)
            return
        }
        //其他全部甩锅网络异常
        ToastUtils.showShort("网络异常")
    }

    public override fun onStart() {
        super.onStart()
        ToastUtils.showShort("http is start")
        // if  NetworkAvailable no !   must to call onCompleted
        if (!isNetworkAvailable(Utils.getContext())) {
            d("无网络，读取缓存数据")
            onComplete()
        }
    }

    override fun onNext(o: T) {
        val baseResponse = o as BaseResponse<*>
        when (baseResponse.code) {
            CodeRule.CODE_200, CodeRule.CODE_220 ->                 // 请求成功, 正确的操作方式, 并消息提示
                //请求成功, 正确的操作方式
                onResult(baseResponse.result as T?)
            CodeRule.CODE_300 -> {
                //请求失败，不打印Message
                e("请求失败")
                ToastUtils.showShort("错误代码:", baseResponse.code)
            }
            CodeRule.CODE_330 ->                 //请求失败，打印Message
                ToastUtils.showShort(baseResponse.message)
            CodeRule.CODE_503 ->                 //参数为空
                e("参数为空")
            CodeRule.CODE_502 ->                 //没有数据
                e("没有数据")
            CodeRule.CODE_510 -> {
                //无效的Token，提示跳入登录页
                ToastUtils.showShort("token已过期，请重新登录")
                //关闭所有页面
                appManager!!.finishAllActivity()
            }
            CodeRule.CODE_530 -> ToastUtils.showShort("请先登录")
            else -> ToastUtils.showShort("错误代码:", baseResponse.code)
        }
    }

    object CodeRule {
        //请求成功, 正确的操作方式
        const val CODE_200 = 200

        //请求成功, 消息提示
        const val CODE_220 = 220

        //请求失败，不打印Message
        const val CODE_300 = 300

        //请求失败，打印Message
        const val CODE_330 = 330

        //服务器内部异常
        const val CODE_500 = 500

        //参数为空
        const val CODE_503 = 503

        //没有数据
        const val CODE_502 = 502

        //无效的Token
        const val CODE_510 = 510

        //未登录
        const val CODE_530 = 530

        //请求的操作异常终止：未知的页面类型
        const val CODE_551 = 551
    }
}