package com.will.habit.ui.form

import android.app.Application
import android.text.TextUtils
import android.view.View
import androidx.databinding.ObservableBoolean
import com.will.habit.entity.FormEntity
import com.will.habit.entity.SpinnerItemData
import com.will.habit.repository.AppRepository
import com.will.habit.ui.base.viewmodel.ToolbarViewModel
import com.google.gson.Gson
import com.will.habit.binding.command.BindingAction
import com.will.habit.binding.command.BindingCommand
import com.will.habit.binding.command.BindingConsumer
import com.will.habit.binding.viewadapter.spinner.IKeyAndValue
import com.will.habit.bus.event.SingleLiveEvent
import com.will.habit.utils.ToastUtils
import java.util.*

/**
 * will
 */
class FormViewModel(application: Application) : ToolbarViewModel<AppRepository>(application) {
    var entity: FormEntity? = null
    var sexItemDatas: MutableList<IKeyAndValue>? = null
    var entityJsonLiveData = SingleLiveEvent<String>()

    //封装一个界面发生改变的观察者
    var uc: UIChangeObservable? = null

    inner class UIChangeObservable {
        //显示日期对话框
        var showDateDialogObservable: ObservableBoolean

        init {
            showDateDialogObservable = ObservableBoolean(false)
        }
    }

    override fun onCreate() {
        super.onCreate()
        uc = UIChangeObservable()
        //sexItemDatas 一般可以从本地Sqlite数据库中取出数据字典对象集合，让该对象实现IKeyAndValue接口
        sexItemDatas = ArrayList()
        (sexItemDatas as ArrayList<IKeyAndValue>).add(SpinnerItemData("男", "1"))
        (sexItemDatas as ArrayList<IKeyAndValue>).add(SpinnerItemData("女", "2"))
    }

    /**
     * 初始化Toolbar
     */
    fun initToolbar() {
        //初始化标题栏
        setRightTextVisible(View.VISIBLE)
        if (TextUtils.isEmpty(entity!!.id)) {
            //ID为空是新增
            setTitleText("表单提交")
        } else {
            //ID不为空是修改
            setTitleText("表单编辑")
        }
    }

    public override fun rightTextOnClick() {
        ToastUtils.showShort("更多")
    }

    fun setFormEntity(entity: FormEntity?) {
        if (this.entity == null) {
            this.entity = entity
        }
    }

    //性别选择的监听
    var onSexSelectorCommand = BindingCommand(object : BindingConsumer<IKeyAndValue> {
        override fun call(t: IKeyAndValue) {

        }

    })



    //生日选择的监听
    var onBirClickCommand = BindingCommand<Any>(object : BindingAction {
        override fun call() {
            //回调到view层(Fragment)中显示日期对话框
            uc!!.showDateDialogObservable.set(!uc!!.showDateDialogObservable.get())
        }
    })

    //是否已婚Switch点状态改变回调
    var onMarryCheckedChangeCommand = BindingCommand(object : BindingConsumer<Boolean> {
        override fun call(isChecked: Boolean) {
            entity!!.marry = isChecked
        }
    })

    //提交按钮点击事件
    var onCmtClickCommand: BindingCommand<*> = BindingCommand<Any?>(object : BindingAction {
        override fun call() {
            val submitJson = Gson().toJson(entity)
            entityJsonLiveData.value = submitJson
        }
    })

    fun setBir(year: Int, month: Int, dayOfMonth: Int) {
        //设置数据到实体中，自动刷新界面
        entity!!.bir = year.toString() + "年" + (month + 1) + "月" + dayOfMonth + "日"
        //刷新实体,驱动界面更新
        entity!!.notifyChange()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}