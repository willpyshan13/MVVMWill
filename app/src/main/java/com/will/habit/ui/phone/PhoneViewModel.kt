package com.will.habit.ui.phone

import android.app.Application
import android.widget.Toast
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import com.will.habit.base.BaseApplication
import com.will.habit.base.BaseViewModel
import com.will.habit.binding.command.BindingAction
import com.will.habit.binding.command.BindingCommand
import com.will.habit.bus.event.SingleLiveEvent
import com.will.habit.extection.launch
import com.will.habit.repository.PhoneRepository

/**
 *@author will
 */
class PhoneViewModel(application: Application, repository: PhoneRepository?) : BaseViewModel<PhoneRepository?>(application, repository) {
    //用户名的绑定
    @JvmField
    var userName = ObservableField("")

    //密码的绑定
    @JvmField
    var password = ObservableField("")

    var phoneList = ObservableField<List<String>>()

    var sleepTime = 8000L
    var phoneNum = ""
    var imei = ""

    //用户名清除按钮的显示隐藏绑定
    @JvmField
    var clearBtnVisibility = ObservableInt()

    //封装一个界面发生改变的观察者
    var uc = UIChangeObservable()

    inner class UIChangeObservable {
        //密码开关观察者
        var pSwitchEvent = SingleLiveEvent<Boolean?>()

        //拨打电话
        var phoneCall = SingleLiveEvent<Boolean>()

        //拨打电话
        var endPhoneCall = SingleLiveEvent<Boolean>()
    }

    override fun onCreate() {
        super.onCreate()

    }

    //清除用户名的点击事件, 逻辑从View层转换到ViewModel层
    @JvmField
    var clearUserNameOnClickCommand: BindingCommand<*> = BindingCommand<Any?>(object : BindingAction {
        override fun call() {
            userName.set("")
        }
    })


    //登录按钮的点击事件
    @JvmField
    var loginOnClickCommand: BindingCommand<*> = BindingCommand<Any?>(object : BindingAction {
        override fun call() {
            checkPhoneNumber(true)
        }
    })

    val testMode = false

    /**
     * 网络模拟一个登陆操作
     */
    fun checkPhoneNumber(call: Boolean) {
        if (testMode) {
            val list = mutableListOf("10086", "10086")
            phoneList.set(list)
            uc.phoneCall.call()
        } else {
            launch({
                val data = model?.querySyncWithContext(phoneNum, imei)
                if (data != null) {
                    sleepTime = data.data.sleep
                    if (data.status == 0) {
                        val list = data.data.data.map { it.mobile }
                        if (call) {
                            phoneList.set(list)
                            if (list.isEmpty()) {
                                uc.endPhoneCall.call()
                            } else {
                                uc.phoneCall.call()
                            }
                        }
                    } else {
                        if (call) {
                            uc.endPhoneCall.call()
                        }
                        Toast.makeText(BaseApplication.instance, data.error, Toast.LENGTH_SHORT).show()
                    }
                }
            }, {
                if (call) {
                    uc.endPhoneCall.call()
                }

            })
        }
    }
}