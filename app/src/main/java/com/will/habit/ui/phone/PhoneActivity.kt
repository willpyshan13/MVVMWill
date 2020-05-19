package com.will.habit.ui.phone

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.tbruyelle.rxpermissions2.RxPermissions
import com.will.habit.BR
import com.will.habit.R
import com.will.habit.app.AppViewModelFactory
import com.will.habit.base.BaseActivity
import com.will.habit.base.BaseApplication
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
        Log.d("call", "onResume  procressPhoneCall")
        procressPhoneCall()
    }

    private fun procressPhoneCall() {
        if (startCalling) {
            Log.d("call", "procressPhoneCall")
            if (viewModel != null && viewModel!!.phoneList.get() != null) {
                if (currentPosition < viewModel!!.phoneList.get()!!.size) {
                    startCalling(viewModel!!.phoneList.get()!![currentPosition])
                } else {
                    Log.d("call", "phone list empty get phone list")
                    viewModel?.checkPhoneNumber(true)
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun startCalling(number: String) {
        Log.d("call", "startCalling")
        currentPosition++
        TelephoneUtils.callPhone(number, this@PhoneActivity)
        Observable.just("").delay(8500, TimeUnit.MILLISECONDS)
                .subscribe(Consumer {
                    Log.d("call", "endCall")
                    TelephoneUtils.endCall(this@PhoneActivity)
                })
    }

    @SuppressLint("CheckResult", "HardwareIds")
    private fun getPhoneNumber() {
        rxPermissions?.request(Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE, Manifest.permission.MODIFY_PHONE_STATE)?.subscribe {
            if (it) {
                val tm = BaseApplication.instance?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                tm.listen(object : PhoneStateListener() {
                    override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                        Log.d("call", "state===$state  phoneNumber==$phoneNumber")
                    }
                }, 0x00020000)
                try {
                    val tel = tm.line1Number //手机号码
                    val imei = tm.imei
                    viewModel?.imei = imei
                    if (tel.isNotEmpty()) {
                        viewModel?.phoneNum = tel.replace("+86", "")
                    } else {
                        viewModel?.phoneNum = tel
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@PhoneActivity, "没有获取到本机号码 请在sim卡中设置", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@PhoneActivity, "请先授权获取号码", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun initData() {
        super.initData()
        rxPermissions = RxPermissions(this)
        getPhoneNumber()
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
            startCalling = true
            currentPosition = 0
            Log.d("call", "initViewObservable procressPhoneCall")
            procressPhoneCall()
        })
        viewModel!!.uc.endPhoneCall.observe(this, Observer {
            startCalling = false
            Observable.just("").delay(3, TimeUnit.SECONDS)
                    .subscribe(Consumer {
                        Log.d("call", "initViewObservable end phone call")
                        viewModel?.checkPhoneNumber(true)
                    })
        })
    }
}