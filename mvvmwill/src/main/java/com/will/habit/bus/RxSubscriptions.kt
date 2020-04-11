package com.will.habit.bus

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * 管理 CompositeSubscription
 */
object RxSubscriptions {
    private val mSubscriptions = CompositeDisposable()
    val isDisposed: Boolean
        get() = mSubscriptions.isDisposed

    @JvmStatic
    fun add(s: Disposable?) {
        if (s != null) {
            mSubscriptions.add(s)
        }
    }

    @JvmStatic
    fun remove(s: Disposable?) {
        if (s != null) {
            mSubscriptions.remove(s)
        }
    }

    fun clear() {
        mSubscriptions.clear()
    }

    fun dispose() {
        mSubscriptions.dispose()
    }
}