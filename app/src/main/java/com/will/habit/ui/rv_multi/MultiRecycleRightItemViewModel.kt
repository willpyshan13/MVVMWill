package com.will.habit.ui.rv_multi

import androidx.databinding.ObservableField
import com.will.habit.base.MultiItemViewModel
import com.will.habit.binding.command.BindingAction
import com.will.habit.binding.command.BindingCommand
import com.will.habit.utils.ToastUtils

/**
 * Create Author：goldze
 * Create Date：2019/01/25
 * Description：
 */
class MultiRecycleRightItemViewModel(viewModel: MultiRecycleViewModel, text: String) : MultiItemViewModel<MultiRecycleViewModel?>(viewModel) {
    @JvmField
    var text = ObservableField("")

    //条目的点击事件
    @JvmField
    var itemClick: BindingCommand<*> = BindingCommand<Any?>(object : BindingAction {
        override fun call() {
            //拿到position
            val position = viewModel.observableList.indexOf(this@MultiRecycleRightItemViewModel)
            ToastUtils.showShort("position：$position")
        }
    })

    init {
        this.text.set(text)
    }
}