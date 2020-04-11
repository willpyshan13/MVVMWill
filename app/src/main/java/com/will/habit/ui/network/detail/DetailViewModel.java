package com.will.habit.ui.network.detail;

import android.app.Application;

import com.will.habit.entity.DemoEntity;
import com.will.habit.repository.AppRepository;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import com.will.habit.base.BaseViewModel;

/**
 * Created by goldze on 2017/7/17.
 */

public class DetailViewModel extends BaseViewModel<AppRepository> {
    public ObservableField<DemoEntity.ItemsEntity> entity = new ObservableField<>();

    public DetailViewModel(@NonNull Application application) {
        super(application);
    }

    public void setDemoEntity(DemoEntity.ItemsEntity entity) {
        this.entity.set(entity);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        entity = null;
    }
}
