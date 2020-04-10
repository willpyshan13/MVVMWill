package com.goldze.mvvmhabit.ui.rv_multi

import androidx.databinding.ObservableField
import me.goldze.mvvmhabit.base.MultiItemViewModel
import me.goldze.mvvmhabit.binding.command.BindingAction
import me.goldze.mvvmhabit.binding.command.BindingCommand
import me.goldze.mvvmhabit.utils.ToastUtils

/**
 * @author will
 */
class MultiRecycleLeftItemViewModel(viewModel: MultiRecycleViewModel, text: String) : MultiItemViewModel<MultiRecycleViewModel?>(viewModel) {
    @JvmField
    var text = ObservableField("")

    //条目的点击事件
    @JvmField
    var itemClick: BindingCommand<*> = BindingCommand<Any?>(object : BindingAction {
        override fun call() {
            //拿到position
            val position = viewModel.observableList.indexOf(this@MultiRecycleLeftItemViewModel)
            ToastUtils.showShort("position：$position")
        }
    })

    init {
        this.text.set(text)
    }
}