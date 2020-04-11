package com.will.habit.ui.tab_bar.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.will.habit.BR;
import com.will.habit.R;

import androidx.annotation.Nullable;
import com.will.habit.base.BaseFragment;

/**
 * Created by goldze on 2018/7/18.
 */

public class TabBar1Fragment extends BaseFragment {
    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_tab_bar_1;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }
}
