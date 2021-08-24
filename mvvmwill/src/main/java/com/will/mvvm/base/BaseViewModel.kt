package com.will.mvvm.base

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.will.mvvm.bus.event.SingleLiveEvent
import java.util.*

/**
 * @author will
 *
 */
open class BaseViewModel<M : BaseModel?> @JvmOverloads constructor(application: Application, @JvmField var model: M? = null) : AndroidViewModel(application), IBaseViewModel {
    private var uc: UIChangeLiveData? = null

    val uC: UIChangeLiveData
        get() {
            if (uc == null) {
                uc = UIChangeLiveData()
            }
            return uc as UIChangeLiveData
        }

    @JvmOverloads
    fun showDialog(title: String = "请稍后...") {
        uc!!.showDialogEvent!!.postValue(title)
    }

    fun dismissDialog() {
        uc!!.dismissDialogEvent!!.call()
    }
    /**
     * 跳转页面
     *
     * @param clz    所跳转的目的Activity类
     * @param bundle 跳转所携带的信息
     */
    /**
     * 跳转页面
     *
     * @param clz 所跳转的目的Activity类
     */
    @JvmOverloads
    fun startActivity(clz: Class<*>, bundle: Bundle? = null) {
        val params: MutableMap<String, Any> = HashMap()
        params[ParameterField.CLASS] = clz
        if (bundle != null) {
            params[ParameterField.BUNDLE] = bundle
        }
        uc!!.startActivityEvent!!.postValue(params)
    }
    /**
     * 跳转容器页面
     *
     * @param canonicalName 规范名 : Fragment.class.getCanonicalName()
     * @param bundle        跳转所携带的信息
     */
    /**
     * 跳转容器页面
     *
     * @param canonicalName 规范名 : Fragment.class.getCanonicalName()
     */
    @JvmOverloads
    fun startContainerActivity(canonicalName: String, bundle: Bundle? = null) {
        val params: MutableMap<String, Any> = HashMap()
        params[ParameterField.CANONICAL_NAME] = canonicalName
        if (bundle != null) {
            params[ParameterField.BUNDLE] = bundle
        }
        uc!!.startContainerActivityEvent!!.postValue(params)
    }

    /**
     * 关闭界面
     */
    fun finish() {
        uc!!.finishEvent!!.call()
    }

    /**
     * 返回上一层
     */
    fun onBackPressed() {
        uc!!.onBackPressedEvent!!.call()
    }

    override fun onAny(owner: LifecycleOwner?, event: Lifecycle.Event?) {}
    override fun onCreate() {}
    override fun onDestroy() {}
    override fun onStart() {}
    override fun onStop() {}
    override fun onResume() {}
    override fun onPause() {}
    override fun registerRxBus() {}
    override fun removeRxBus() {}
    override fun onCleared() {
        super.onCleared()
        model?.let {
            model!!.onCleared()
        }
    }

    inner class UIChangeLiveData : SingleLiveEvent<Any?>() {
        var showDialogEvent: SingleLiveEvent<String>? = null
            get() = createLiveData(field).also { field = it }
            private set
        var dismissDialogEvent: SingleLiveEvent<Void>? = null
            get() = createLiveData(field).also { field = it }
            private set
        var startActivityEvent: SingleLiveEvent<Map<String, Any>>? = null
            get() = createLiveData(field).also { field = it }
            private set
        var startContainerActivityEvent: SingleLiveEvent<Map<String, Any>>? = null
            get() = createLiveData(field).also { field = it }
            private set
        var finishEvent: SingleLiveEvent<Void>? = null
            get() = createLiveData(field).also { field = it }
            private set
        var onBackPressedEvent: SingleLiveEvent<Void>? = null
            get() = createLiveData(field).also { field = it }
            private set

        private fun <T> createLiveData(liveData: SingleLiveEvent<T>?): SingleLiveEvent<T> {
            var liveData = liveData
            if (liveData == null) {
                liveData = SingleLiveEvent()
            }
            return liveData
        }

        override fun observe(owner: LifecycleOwner, observer: Observer<in Any?>) {
            super.observe(owner, observer)
        }
    }

    object ParameterField {
        @JvmField
        var CLASS = "CLASS"
        @JvmField
        var CANONICAL_NAME = "CANONICAL_NAME"
        @JvmField
        var BUNDLE = "BUNDLE"
    }
}