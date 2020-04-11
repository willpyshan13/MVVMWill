package com.will.habit.ui.rv_multi;

import androidx.annotation.NonNull;
import com.will.habit.base.BaseViewModel;
import com.will.habit.base.MultiItemViewModel;
import com.will.habit.binding.command.BindingAction;
import com.will.habit.binding.command.BindingCommand;
import com.will.habit.utils.ToastUtils;

/**
 * Create Author：goldze
 * Create Date：2019/01/25
 * Description：
 */

public class MultiRecycleHeadViewModel extends MultiItemViewModel {

    public MultiRecycleHeadViewModel(@NonNull BaseViewModel viewModel) {
        super(viewModel);
    }

    //条目的点击事件
    public BindingCommand itemClick = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            ToastUtils.showShort("我是头布局");
        }
    });
}
