package com.will.mvvm.ui.login

import android.os.Bundle
import com.will.mvvm.BR
import com.will.mvvm.R
import com.will.mvvm.base.BaseActivity
import com.will.mvvm.databinding.ActivityLoginBinding

/**
 *
 * <p>登录界面
 * Date: 2021-08-24
 * Company: xmotion
 * Updater:
 * Update Time:
 * Update Comments:
 *
 * Author: will
 */
class LoginActivity : BaseActivity<ActivityLoginBinding, LoginViewModel>() {
    //ActivityLoginBinding类是databinding框架自定生成的,对应activity_login.xml
    override fun initContentView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_login
    }

    override fun initVariableId(): Int {
        return BR.viewModel
    }

    override fun initViewObservable() {

    }
}