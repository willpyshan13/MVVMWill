package com.will.habit.ui.phone

import android.app.Application
import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import com.will.habit.base.BaseApplication
import com.will.habit.base.BaseViewModel
import com.will.habit.binding.command.BindingAction
import com.will.habit.binding.command.BindingCommand
import com.will.habit.binding.command.BindingConsumer
import com.will.habit.bus.event.SingleLiveEvent
import com.will.habit.extection.launch
import com.will.habit.repository.PhoneRepository
import com.will.habit.utils.ToastUtils
import kotlinx.coroutines.CoroutineScope

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
            login()
        }
    })

    /**
     * 网络模拟一个登陆操作
     */
    fun login() {
//        uc.phoneCall.call()
        launch({
            val tm = BaseApplication.instance?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val deviceid = tm.deviceId //获取ID号
            val tel = tm.line1Number //手机号码
            val imei = tm.simSerialNumber
            val imsi = tm.subscriberId
            val simState = tm.simState
            val data = model?.querySyncWithContext(tel.replace("+86", ""))
            if (data?.data?.data != null) {
                val list = data.data.data.map { it.mobile }
                phoneList.set(list)
                if (list.isEmpty()){
                    uc.endPhoneCall.call()
                }else{
                    uc.phoneCall.call()
                }
            } else {
                uc.endPhoneCall.call()
            }
        }, {
            uc.endPhoneCall.call()
            ToastUtils.showShort("获取失败")
        })
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}