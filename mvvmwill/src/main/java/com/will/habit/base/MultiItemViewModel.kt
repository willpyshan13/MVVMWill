package com.will.habit.base

/**
 * @author will
 *
 */
open class MultiItemViewModel<VM : BaseViewModel<*>?>(viewModel: VM) : ItemViewModel<VM>(viewModel) {
    var itemType: Any? = null
        protected set

    fun multiItemType(multiType: Any) {
        itemType = multiType
    }
}