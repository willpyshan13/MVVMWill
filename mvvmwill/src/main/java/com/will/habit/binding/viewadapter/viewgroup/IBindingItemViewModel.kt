package com.will.habit.binding.viewadapter.viewgroup

import androidx.databinding.ViewDataBinding

/**
 * Created by goldze on 2017/6/15.
 */
interface IBindingItemViewModel<V : ViewDataBinding?> {
    fun injecDataBinding(binding: V)
}