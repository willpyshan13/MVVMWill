package com.will.habit.binding.viewadapter.viewgroup

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableList
import androidx.databinding.ViewDataBinding
import me.tatarka.bindingcollectionadapter2.ItemBinding

/**
 * @author will
 */
object ViewAdapter {
    @BindingAdapter("itemView", "observableList")
    fun addViews(viewGroup: ViewGroup, itemBinding: ItemBinding<*>, viewModelList: ObservableList<IBindingItemViewModel<*>>?) {
        if (viewModelList != null && !viewModelList.isEmpty()) {
            viewGroup.removeAllViews()
            for (viewModel in viewModelList) {
                val binding = DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(viewGroup.context),
                        itemBinding.layoutRes(), viewGroup, true)
                binding.setVariable(itemBinding.variableId(), viewModel)
                viewModel.injecDataBinding(binding as Nothing)
            }
        }
    }
}