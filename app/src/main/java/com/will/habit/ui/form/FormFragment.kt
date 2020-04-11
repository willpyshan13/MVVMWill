package com.will.habit.ui.form

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.Observable
import androidx.databinding.Observable.OnPropertyChangedCallback
import androidx.lifecycle.Observer
import com.will.habit.BR
import com.will.habit.R
import com.will.habit.databinding.FragmentFormBinding
import com.will.habit.entity.FormEntity
import com.will.habit.base.BaseFragment
import com.will.habit.utils.MaterialDialogUtils
import java.util.*

/**
 * @author will
 * 表单提交/编辑界面
 */
class FormFragment : BaseFragment<FragmentFormBinding, FormViewModel>() {
    private var entity: FormEntity? = FormEntity()
    override fun initParam() {
        //获取列表传入的实体
        val mBundle = arguments
        if (mBundle != null) {
            entity = mBundle.getParcelable("entity")
        }
    }

    override fun initContentView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): Int {
        return R.layout.fragment_form
    }

    override fun initVariableId(): Int {
        return BR.viewModel
    }

    override fun initData() {
        //通过binding拿到toolbar控件, 设置给Activity
        (activity as AppCompatActivity?)!!.setSupportActionBar(binding!!.include.toolbar)
        //View层传参到ViewModel层
        viewModel?.setFormEntity(entity)
        //初始化标题
        viewModel?.initToolbar()
    }

    override fun initViewObservable() {
        //监听日期选择
        viewModel!!.uc?.showDateDialogObservable?.addOnPropertyChangedCallback(object : OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable, propertyId: Int) {
                val calendar = Calendar.getInstance()
                val year = calendar[Calendar.YEAR]
                val month = calendar[Calendar.MONTH]
                val day = calendar[Calendar.DAY_OF_MONTH]
                val datePickerDialog = DatePickerDialog(context!!, OnDateSetListener { view, year, month, dayOfMonth -> viewModel!!.setBir(year, month, dayOfMonth) }, year, month, day)
                datePickerDialog.setMessage("生日选择")
                datePickerDialog.show()
            }
        })
        viewModel!!.entityJsonLiveData.observe(this, Observer { submitJson -> MaterialDialogUtils.showBasicDialog(context, "提交的json实体数据：\r\n$submitJson").show() })
    }
}