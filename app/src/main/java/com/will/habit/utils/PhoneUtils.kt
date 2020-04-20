package com.will.habit.utils

import android.content.Context
import android.os.IBinder
import android.telecom.TelecomManager
import android.util.Log
import com.android.internal.telephony.ITelephony


/**
 * Desc:
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
    @JvmStatic
    fun rejectCall(context:Context) {
        try {

            val tm = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

            if (tm != null) {
                val success = tm.endCall()
                // success == true if call was terminated.
            }

            val method = Class.forName("android.os.ServiceManager")
                    .getMethod("getService", String::class.java)
            val binder = method.invoke(null, *arrayOf<Any>(Context.TELEPHONY_SERVICE)) as IBinder
            val telephony: ITelephony = ITelephony.Stub.asInterface(binder)
            telephony.endCall()
        } catch (e: NoSuchMethodException) {
            Log.d("","NoSuchMethodException")
        } catch (e: ClassNotFoundException) {
            Log.d("","ClassNotFoundException")
        } catch (e: Exception) {
            Log.d("","Exception")
        }
    }
}