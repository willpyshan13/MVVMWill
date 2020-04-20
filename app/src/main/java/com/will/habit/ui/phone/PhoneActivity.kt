package com.will.habit.ui.phone

import android.Manifest
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.tbruyelle.rxpermissions2.RxPermissions
import com.will.habit.BR
import com.will.habit.R
import com.will.habit.app.AppViewModelFactory
import com.will.habit.base.BaseActivity
import com.will.habit.databinding.ActivityPhoneBinding
import com.will.habit.utils.TelephoneUtils
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import java.util.concurrent.TimeUnit


/**
 * 一个MVVM模式的登陆界面
 */
class PhoneActivity : BaseActivity<ActivityPhoneBinding, PhoneViewModel>() {
    //ActivityLoginBinding类是databinding框架自定生成的,对应activity_login.xml
    var rxPermissions: RxPermissions? = null
    override fun initContentView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_phone
    }

    var currentPosition = 0

    var startCalling = false

    override fun onResume() {
        super.onResume()
        procressPhoneCall()
    }

    private fun procressPhoneCall(){
        if (startCalling) {
            if (viewModel!=null&&viewModel!!.phoneList.get()!=null) {
                if (currentPosition < viewModel!!.phoneList.get()!!.size) {
                    startCalling(viewModel!!.phoneList.get()!![currentPosition])
                    currentPosition++
                }else{
                    viewModel?.login()
                }
            }
        }
    }

    private fun startCalling(number:String){
        currentPosition++
        TelephoneUtils.callPhone(number, this@PhoneActivity)
        Observable.just("").delay(7, TimeUnit.SECONDS)
                .subscribe(Consumer {
                    TelephoneUtils.endCall(this@PhoneActivity)
                })
    }

    override fun initData() {
        super.initData()
        rxPermissions = RxPermissions(this)
    }

    override fun initVariableId(): Int {
        return BR.viewModel
    }

    override fun initViewModel(): PhoneViewModel? {
        //使用自定义的ViewModelFactory来创建ViewModel，如果不重写该方法，则默认会调用LoginViewModel(@NonNull Application application)构造方法
        val factory = AppViewModelFactory.getInstance(application)
        return ViewModelProviders.of(this, factory).get(PhoneViewModel::class.java)
    }

    override fun initViewObservable() {
        viewModel!!.uc.phoneCall.observe(this, Observer {
            rxPermissions?.request(Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE, Manifest.permission.MODIFY_PHONE_STATE)?.subscribe {
                startCalling = true
                currentPosition = 0
            }
        })
        viewModel!!.uc.endPhoneCall.observe(this, Observer {
            startCalling = false
            Observable.just("").delay(3, TimeUnit.SECONDS)
                    .subscribe(Consumer {
                        viewModel?.login()
                    })
        })
    }
}