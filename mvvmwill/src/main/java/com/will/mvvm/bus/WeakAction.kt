package com.will.mvvm.bus

import com.will.mvvm.binding.command.BindingAction
import com.will.mvvm.binding.command.BindingConsumer
import java.lang.ref.WeakReference

/**
 * About : kelin的WeakBindingAction
 */
class WeakAction<T> {
    var bindingAction: BindingAction? = null
        private set
    private var consumer: BindingConsumer<T>? = null
    val isLive: Boolean
        get() {
            if (reference == null) {
                return false
            }
            return (reference as WeakReference<*>).get() != null
        }
    val target: Any?
        get() = if (reference != null) {
            (reference as WeakReference<*>).get()
        } else null
    private var reference: WeakReference<*>?

    constructor(target: Any?, action: BindingAction?) {
        reference = WeakReference(target)
        bindingAction = action
    }

    constructor(target: Any?, consumer: BindingConsumer<T>?) {
        reference = WeakReference(target)
        this.consumer = consumer
    }

    fun execute() {
        if (bindingAction != null && isLive) {
            bindingAction!!.call()
        }
    }

    fun execute(parameter: T) {
        if (consumer != null
                && isLive) {
            consumer?.call(parameter)
        }
    }

    fun markForDeletion() {
        reference!!.clear()
        reference = null
        bindingAction = null
        consumer = null
    }

    val bindingConsumer: BindingConsumer<*>?
        get() = consumer

}