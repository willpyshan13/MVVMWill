package com.will.habit.http

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.telephony.TelephonyManager
import java.io.IOException
import java.net.HttpURLConnection
import java.net.NetworkInterface
import java.net.SocketException
import java.net.URL

object NetworkUtil {
    @JvmField
    var url = "http://www.baidu.com"
    var NET_CNNT_BAIDU_OK = 1 // NetworkAvailable
    var NET_CNNT_BAIDU_TIMEOUT = 2 // no NetworkAvailable
    var NET_NOT_PREPARE = 3 // Net no ready
    var NET_ERROR = 4 //net error
    private const val TIMEOUT = 3000 // TIMEOUT

    /**
     * check NetworkAvailable
     * @param context
     * @return
     */
    @JvmStatic
    fun isNetworkAvailable(context: Context): Boolean {
        val manager = context.applicationContext.getSystemService(
                Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                ?: return false
        val info = manager.activeNetworkInfo
        return if (null == info || !info.isAvailable) false else true
    }

    /**
     * getLocalIpAddress
     * @return
     */
    val localIpAddress: String
        get() {
            var ret = ""
            try {
                val en = NetworkInterface.getNetworkInterfaces()
                while (en.hasMoreElements()) {
                    val intf = en.nextElement()
                    val enumIpAddr = intf.inetAddresses
                    while (enumIpAddr.hasMoreElements()) {
                        val inetAddress = enumIpAddr.nextElement()
                        if (!inetAddress.isLoopbackAddress) {
                            ret = inetAddress.hostAddress.toString()
                        }
                    }
                }
            } catch (ex: SocketException) {
                ex.printStackTrace()
            }
            return ret
        }

    /**
     * 返回当前网络状态
     *
     * @param context
     * @return
     */
    fun getNetState(context: Context): Int {
        try {
            val connectivity = context
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (connectivity != null) {
                val networkinfo = connectivity.activeNetworkInfo
                if (networkinfo != null) {
                    return if (networkinfo.isAvailable && networkinfo.isConnected) {
                        if (!connectionNetwork()) NET_CNNT_BAIDU_TIMEOUT else NET_CNNT_BAIDU_OK
                    } else {
                        NET_NOT_PREPARE
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return NET_ERROR
    }

    /**
     * ping "http://www.baidu.com"
     * @return
     */
    private fun connectionNetwork(): Boolean {
        var result = false
        var httpUrl: HttpURLConnection? = null
        try {
            httpUrl = URL(url)
                    .openConnection() as HttpURLConnection
            httpUrl.connectTimeout = TIMEOUT
            httpUrl!!.connect()
            result = true
        } catch (e: IOException) {
        } finally {
            httpUrl?.disconnect()
            httpUrl = null
        }
        return result
    }

    /**
     * isWifi
     * @param context
     * @return boolean
     */
    fun isWifi(context: Context): Boolean {
        val connectivityManager = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetInfo = connectivityManager.activeNetworkInfo
        return if (activeNetInfo != null
                && activeNetInfo.type == ConnectivityManager.TYPE_WIFI) {
            true
        } else false
    }

    /**
     * is2G
     * @param context
     * @return boolean
     */
    fun is2G(context: Context): Boolean {
        val connectivityManager = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetInfo = connectivityManager.activeNetworkInfo
        return if (activeNetInfo != null
                && (activeNetInfo.subtype == TelephonyManager.NETWORK_TYPE_EDGE || activeNetInfo.subtype == TelephonyManager.NETWORK_TYPE_GPRS || activeNetInfo
                        .subtype == TelephonyManager.NETWORK_TYPE_CDMA)) {
            true
        } else false
    }
}