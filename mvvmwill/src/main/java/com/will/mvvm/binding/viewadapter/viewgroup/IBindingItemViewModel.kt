package com.will.mvvm.binding.viewadapter.viewgroup

import androidx.databinding.ViewDataBinding

interface IBindingItemViewModel<V : ViewDataBinding?> {
    fun injecDataBinding(binding: V)
}