package com.will.habit.ui.login

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.will.habit.BR
import com.will.habit.R
import com.will.habit.app.AppViewModelFactory
import com.will.habit.base.BaseActivity
import com.will.habit.databinding.ActivityLoginBinding

/**
 * 一个MVVM模式的登陆界面
 */
class LoginActivity : BaseActivity<ActivityLoginBinding, LoginViewModel>() {
    //ActivityLoginBinding类是databinding框架自定生成的,对应activity_login.xml
    override fun initContentView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_login
    }

    override fun initVariableId(): Int {
        return BR.viewModel
    }

    override fun initViewModel(): LoginViewModel? {
        //使用自定义的ViewModelFactory来创建ViewModel，如果不重写该方法，则默认会调用LoginViewModel(@NonNull Application application)构造方法
        val factory = AppViewModelFactory.getInstance(application)
        return ViewModelProviders.of(this, factory).get(LoginViewModel::class.java)
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
    }
}