package com.will.habit.utils

import android.content.Context
import android.os.IBinder
import android.telephony.TelephonyManager
import android.util.Log


/**
 * Desc:电话号码工具类
 *
 * Date: 2020-04-20
 * Copyright: Copyright (c) 2018-2020
 * Company: @微微科技有限公司
 * Updater:
 * Update Time:
 * Update Comments:
 *
 * @Author: pengyushan
 */
object PhoneUtils {

    fun getPhoneNum(context: Context):String {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val deviceid = tm.deviceId //获取ID号
        val tel = tm.line1Number //手机号码
        val imei = tm.simSerialNumber
        val imsi = tm.subscriberId
        val simState = tm.simState
        return ""
    }
}