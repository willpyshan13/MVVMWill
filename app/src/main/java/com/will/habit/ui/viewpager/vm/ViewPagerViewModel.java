package com.will.habit.ui.viewpager.vm;

import android.app.Application;

import com.will.habit.BR;
import com.will.habit.R;
import com.will.habit.data.DemoRepository;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;
import com.will.habit.base.BaseViewModel;
import com.will.habit.binding.command.BindingCommand;
import com.will.habit.binding.command.BindingConsumer;
import com.will.habit.bus.event.SingleLiveEvent;
import com.will.habit.utils.ToastUtils;
import me.tatarka.bindingcollectionadapter2.BindingViewPagerAdapter;
import me.tatarka.bindingcollectionadapter2.ItemBinding;

/**
 * 所有例子仅做参考,千万不要把它当成一种标准,毕竟主打的不是例子,业务场景繁多,理解如何使用才最重要。
 * Created by goldze on 2018/7/18.
 */

public class ViewPagerViewModel extends BaseViewModel<DemoRepository> {
    public SingleLiveEvent<String> itemClickEvent = new SingleLiveEvent<>();
    public ViewPagerViewModel(@NonNull Application application) {
        super(application);
        //模拟3个ViewPager页面
        for (int i = 1; i <= 3; i++) {
            ViewPagerItemViewModel itemViewModel = new ViewPagerItemViewModel(this, "第" + i + "个页面");
            items.add(itemViewModel);
        }
    }

    //给ViewPager添加ObservableList
    public ObservableList<ViewPagerItemViewModel> items = new ObservableArrayList<>();
    //给ViewPager添加ItemBinding
    public ItemBinding<ViewPagerItemViewModel> itemBinding = ItemBinding.of(BR.viewModel, R.layout.item_viewpager);
    //给ViewPager添加PageTitle
    public final BindingViewPagerAdapter.PageTitles<ViewPagerItemViewModel> pageTitles = new BindingViewPagerAdapter.PageTitles<ViewPagerItemViewModel>() {
        @Override
        public CharSequence getPageTitle(int position, ViewPagerItemViewModel item) {
            return "条目" + position;
        }
    };
    //ViewPager切换监听
    public BindingCommand<Integer> onPageSelectedCommand = new BindingCommand<>(new BindingConsumer<Integer>() {
        @Override
        public void call(Integer index) {
            ToastUtils.showShort("ViewPager切换：" + index);
        }
    });
}
