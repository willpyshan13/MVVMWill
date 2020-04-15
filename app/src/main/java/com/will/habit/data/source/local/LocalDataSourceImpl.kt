package com.will.habit.data.source.local

import com.will.habit.data.source.LocalDataSource
import com.will.habit.utils.SPUtils

/**
 * 本地数据源，可配合Room框架使用
 * Created by goldze on 2019/3/26.
 */
class LocalDataSourceImpl private constructor() : LocalDataSource {
    private val spName = "name"
    override fun saveUserName(userName: String) {
        SPUtils.getInstance(spName).put("UserName", userName)
    }

    override fun savePassword(password: String) {
        SPUtils.getInstance(spName).put("password", password)
    }

    override fun getUserName(): String {
        return SPUtils.getInstance(spName).getString("UserName")!!
    }

    override fun getPassword(): String {
        return SPUtils.getInstance(spName).getString("password")!!
    }

    companion object {
        @Volatile
        private var INSTANCE: LocalDataSourceImpl? = null
        @JvmStatic
        val instance: LocalDataSourceImpl?
            get() {
                if (INSTANCE == null) {
                    synchronized(LocalDataSourceImpl::class.java) {
                        if (INSTANCE == null) {
                            INSTANCE = LocalDataSourceImpl()
                        }
                    }
                }
                return INSTANCE
            }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}