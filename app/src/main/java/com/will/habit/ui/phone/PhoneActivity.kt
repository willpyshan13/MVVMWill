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
        if (startCalling) {
            if (viewModel!=null&&viewModel!!.phoneList.get()!=null) {
                if (currentPosition < viewModel!!.phoneList.get()!!.size) {
                    startCalling(viewModel!!.phoneList.get()!![currentPosition])
                    currentPosition++
                }else{
                    //
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
        //监听ViewModel中pSwitchObservable的变化, 当ViewModel中执行【uc.pSwitchObservable.set(!uc.pSwitchObservable.get());】时会回调该方法
        viewModel!!.uc.pSwitchEvent.observe(this, Observer {
            //pSwitchObservable是boolean类型的观察者,所以可以直接使用它的值改变密码开关的图标
            if (viewModel!!.uc.pSwitchEvent.value!!) {
                //密码可见
                //在xml中定义id后,使用binding可以直接拿到这个view的引用,不再需要findViewById去找控件了
                binding!!.ivSwichPasswrod.setImageResource(R.mipmap.show_psw)
                binding!!.etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                //密码不可见
                binding!!.ivSwichPasswrod.setImageResource(R.mipmap.show_psw_press)
                binding!!.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        })

        viewModel!!.uc.phoneCall.observe(this, Observer {
            rxPermissions?.request(Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE, Manifest.permission.MODIFY_PHONE_STATE)?.subscribe {
                startCalling
            }
        })
    }
}