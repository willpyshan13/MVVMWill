package com.will.mvvm.binding.viewadapter.checkbox

import android.widget.CheckBox
import androidx.databinding.BindingAdapter
import com.will.mvvm.binding.command.BindingCommand

/**
 * @author will
 */
object ViewAdapter {
    /**
     * @param bindingCommand //绑定监听
     */
    @BindingAdapter(value = ["onCheckedChangedCommand"], requireAll = false)
    fun setCheckedChanged(checkBox: CheckBox, bindingCommand: BindingCommand<Boolean?>) {
        checkBox.setOnCheckedChangeListener { compoundButton, b -> bindingCommand.execute(b) }
    }
}