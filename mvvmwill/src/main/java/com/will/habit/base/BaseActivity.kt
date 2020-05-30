package com.will.habit.base

import android.content.Intent
import android.os.Bundle
import androidx.annotation.NonNull
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import com.will.habit.base.BaseViewModel.ParameterField
import com.will.habit.base.ContainerActivity
import com.will.habit.bus.Messenger
import com.will.habit.utils.MaterialDialogUtils
import java.lang.reflect.ParameterizedType

/**
 * @author will
 */
abstract class BaseActivity<V : ViewDataBinding, VM : BaseViewModel<*>> : RxAppCompatActivity(), IBaseView {
    protected lateinit var binding: V
    protected lateinit var viewModel: VM
    private var viewModelId = 0
    private var dialog: MaterialDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //页面接受的参数方法
        initParam()
        //私有的初始化Databinding和ViewModel方法
        initViewDataBinding(savedInstanceState)
        //私有的ViewModel与View的契约事件回调逻辑
        registorUIChangeLiveDataCallBack()
        //页面数据初始化方法
        initData()
        //页面事件监听的方法，一般用于ViewModel层转到View层的事件注册
        initViewObservable()
        //注册RxBus
        viewModel!!.registerRxBus()
    }

    override fun onDestroy() {
        super.onDestroy()
        //解除Messenger注册
        Messenger.getDefault().unregister(viewModel)
        if (viewModel != null) {
            viewModel?.removeRxBus()
        }
        if (binding != null) {
            binding?.unbind()
        }
    }

    /**
     * 注入绑定
     */
    private fun initViewDataBinding(savedInstanceState: Bundle?) {
        //DataBindingUtil类需要在project的build中配置 dataBinding {enabled true }, 同步后会自动关联android.databinding包
        binding = DataBindingUtil.setContentView(this, initContentView(savedInstanceState))
        viewModelId = initVariableId()
        var viewModel = initViewModel()
        if (viewModel == null) {
            val modelClass: Class<BaseViewModel<*>>
            val type = javaClass.genericSuperclass
            modelClass = if (type is ParameterizedType){
                type.actualTypeArguments[1] as Class<BaseViewModel<*>>
            }else{
                BaseViewModel::class.java
            }
            viewModel = createViewModel(this, modelClass ) as VM
        }
        this.viewModel = viewModel
        //关联ViewModel
        binding?.setVariable(viewModelId, viewModel)
        //支持LiveData绑定xml，数据改变，UI自动会更新
        binding?.lifecycleOwner = this
        //让ViewModel拥有View的生命周期感应
        viewModel?.let {
            lifecycle.addObserver(viewModel!!)
        }

        //注入RxLifecycle生命周期
        viewModel!!.injectLifecycleProvider(this)
    }

    //刷新布局
    fun refreshLayout() {
        if (viewModel != null) {
            binding!!.setVariable(viewModelId, viewModel)
        }
    }

    /**
     * =====================================================================
     */
    //注册ViewModel与View的契约UI回调事件
    protected fun registorUIChangeLiveDataCallBack() {
        //加载对话框显示
        viewModel!!.uC.showDialogEvent!!.observe(this, Observer { title -> showDialog(title) })
        //加载对话框消失
        viewModel!!.uC.dismissDialogEvent!!.observe(this, Observer { dismissDialog() })
        //跳入新页面
        viewModel!!.uC.startActivityEvent!!.observe(this, Observer {
            val clz = it!![ParameterField.CLASS] as Class<*>?
            val bundle = it[ParameterField.BUNDLE] as Bundle?
            startActivity(clz, bundle)
        })
        //跳入ContainerActivity
        viewModel!!.uC.startContainerActivityEvent!!.observe(this, Observer {
            val canonicalName = it!![ParameterField.CANONICAL_NAME] as String?
            val bundle = it[ParameterField.BUNDLE] as Bundle?
            startContainerActivity(canonicalName, bundle)
        })
        //关闭界面
        viewModel!!.uC.finishEvent!!.observe(this, Observer { finish() })
        //关闭上一层
        viewModel!!.uC.onBackPressedEvent!!.observe(this, Observer { onBackPressed() })
    }

    fun showDialog(title: String?) {
        if (dialog != null) {
            dialog = dialog!!.builder.title(title!!).build()
            dialog?.show()
        } else {
            val builder = MaterialDialogUtils.showIndeterminateProgressDialog(this, title, true)
            dialog = builder.show()
        }
    }

    fun dismissDialog() {
        if (dialog != null && dialog!!.isShowing) {
            dialog!!.dismiss()
        }
    }

    /**
     * 跳转页面
     *
     * @param clz 所跳转的目的Activity类
     */
    fun startActivity(clz: Class<*>?) {
        startActivity(Intent(this, clz))
    }

    /**
     * 跳转页面
     *
     * @param clz    所跳转的目的Activity类
     * @param bundle 跳转所携带的信息
     */
    fun startActivity(clz: Class<*>?, bundle: Bundle?) {
        val intent = Intent(this, clz)
        if (bundle != null) {
            intent.putExtras(bundle)
        }
        startActivity(intent)
    }
    /**
     * 跳转容器页面
     *
     * @param canonicalName 规范名 : Fragment.class.getCanonicalName()
     * @param bundle        跳转所携带的信息
     */
    /**
     * 跳转容器页面
     *
     * @param canonicalName 规范名 : Fragment.class.getCanonicalName()
     */
    @JvmOverloads
    fun startContainerActivity(canonicalName: String?, bundle: Bundle? = null) {
        val intent = Intent(this, ContainerActivity::class.java)
        intent.putExtra(ContainerActivity.FRAGMENT, canonicalName)
        if (bundle != null) {
            intent.putExtra(ContainerActivity.BUNDLE, bundle)
        }
        startActivity(intent)
    }

    /**
     * =====================================================================
     */
    override fun initParam() {}

    /**
     * 初始化根布局
     *
     * @return 布局layout的id
     */
    abstract fun initContentView(savedInstanceState: Bundle?): Int

    /**
     * 初始化ViewModel的id
     *
     * @return BR的id
     */
    abstract fun initVariableId(): Int

    /**
     * 初始化ViewModel
     *
     * @return 继承BaseViewModel的ViewModel
     */
    open fun initViewModel(): VM? {
        return null
    }

    override fun initData() {}
    override fun initViewObservable() {}

    /**
     * 创建ViewModel
     *
     * @param cls
     * @param <T>
     * @return
    </T> */
    fun <T : ViewModel?> createViewModel(activity: FragmentActivity?, cls: Class<T>): T {
        return ViewModelProviders.of(activity!!).get(cls)
    }
}