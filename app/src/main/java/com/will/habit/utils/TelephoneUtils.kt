package com.will.habit.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.Log
import com.android.internal.telephony.ITelephony

object TelephoneUtils {

    /**
     * 拨打电话
     */
    fun callPhone(phoneNum:String,context:Context){
        val intent = Intent(Intent.ACTION_CALL)
        val data = Uri.parse("tel:" + phoneNum)
        intent.data = data
        context.startActivity(intent)
    }

    /**
     * 结束电话
     */
    fun endCall(context: Context) {
        var telephonyService: ITelephony? = null
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        try {
            val c = Class.forName(tm.javaClass.name)
            val m = c.getDeclaredMethod("getITelephony")
            m.isAccessible = true
            telephonyService = m.invoke(tm) as ITelephony
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            telecomManager.endCall()
        } else {
            if (telephonyService != null) {
                try {
                    val success = telephonyService.endCall()
                    Log.d("", "success=$success")
                    if (success) {
                    }
                } catch (e: Exception) {
                    Log.d("", "success=${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }
}