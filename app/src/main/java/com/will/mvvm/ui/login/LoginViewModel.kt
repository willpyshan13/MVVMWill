package com.will.mvvm.ui.login

import android.app.Application
import com.will.mvvm.base.BaseViewModel
import com.will.mvvm.binding.command.BindingAction
import com.will.mvvm.binding.command.BindingCommand
import com.will.mvvm.repository.AppRepository
import com.will.mvvm.utils.KLog

class LoginViewModel(application: Application) : BaseViewModel<AppRepository>(application) {

    val edittextClick = BindingCommand<Any>(object : BindingAction {
        override fun call() {
            KLog.d("edittextClick")
        }
    })
}