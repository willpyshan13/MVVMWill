package com.will.mvvm.base

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import java.lang.reflect.ParameterizedType

/**
 * @author will
 */
abstract class BaseFragment<V : ViewDataBinding, VM : BaseViewModel<*>> : Fragment(), IBaseView {
    protected lateinit var binding: V
    protected lateinit var viewModel: VM
    private var viewModelId = 0
    private var dialog: MaterialDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initParam()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, initContentView(inflater, container, savedInstanceState), container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //解除Messenger注册
        com.will.mvvm.bus.Messenger.getDefault().unregister(viewModel)
        viewModel.removeRxBus()
        if (::binding.isInitialized) {
            binding.unbind()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //私有的初始化Databinding和ViewModel方法
        initViewDataBinding()
        //私有的ViewModel与View的契约事件回调逻辑
        registorUIChangeLiveDataCallBack()
        //页面数据初始化方法
        initData()
        //页面事件监听的方法，一般用于ViewModel层转到View层的事件注册
        initViewObservable()
        //注册RxBus
        viewModel.registerRxBus()
    }

    /**
     * 注入绑定
     */
    private fun initViewDataBinding() {
        viewModelId = initVariableId()
        var viewModel = initViewModel()
        if (viewModel == null) {
            val modelClass: Class<BaseViewModel<*>>
            val type = javaClass.genericSuperclass
            modelClass = if (type is ParameterizedType) {
                type.actualTypeArguments[1] as Class<BaseViewModel<*>>
            } else {
                //如果没有指定泛型参数，则默认使用BaseViewModel
                BaseViewModel::class.java
            }
            viewModel = createViewModel(this, modelClass) as VM
        }
        this.viewModel = viewModel
        binding.setVariable(viewModelId, viewModel)
        //支持LiveData绑定xml，数据改变，UI自动会更新
        binding.lifecycleOwner = this
        //让ViewModel拥有View的生命周期感应
        this.viewModel.let {
            lifecycle.addObserver(this.viewModel)
        }
    }

    /**
     * =====================================================================
     */
    //注册ViewModel与View的契约UI回调事件
    protected fun registorUIChangeLiveDataCallBack() {
        //加载对话框显示
        viewModel.uC.showDialogEvent!!.observe(requireActivity(), Observer { title -> showDialog(title) })
        //加载对话框消失
        viewModel.uC.dismissDialogEvent!!.observe(requireActivity(), Observer { dismissDialog() })
        //跳入新页面
        viewModel.uC.startActivityEvent!!.observe(requireActivity(), Observer {
            val clz = it!![BaseViewModel.ParameterField.CLASS] as Class<*>?
            val bundle = it[BaseViewModel.ParameterField.BUNDLE] as Bundle?
            startActivity(clz, bundle)
        })
        //跳入ContainerActivity
        viewModel.uC.startContainerActivityEvent!!.observe(requireActivity(), Observer {
            val canonicalName = it!![BaseViewModel.ParameterField.CANONICAL_NAME] as String?
            val bundle = it[BaseViewModel.ParameterField.BUNDLE] as Bundle?
            startContainerActivity(canonicalName, bundle)
        })
        //关闭界面
        viewModel.uC.finishEvent!!.observe(requireActivity(), Observer { activity!!.finish() })
        //关闭上一层
        viewModel.uC.onBackPressedEvent!!.observe(requireActivity(), Observer { activity!!.onBackPressed() })
    }

    fun showDialog(title: String?) {
        if (dialog != null) {
            dialog = dialog!!.builder.title(title!!).build()
            dialog?.show()
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
        startActivity(Intent(context, clz))
    }

    /**
     * 跳转页面
     *
     * @param clz    所跳转的目的Activity类
     * @param bundle 跳转所携带的信息
     */
    fun startActivity(clz: Class<*>?, bundle: Bundle?) {
        val intent = Intent(context, clz)
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
        val intent = Intent(context, com.will.mvvm.base.ContainerActivity::class.java)
        intent.putExtra(com.will.mvvm.base.ContainerActivity.FRAGMENT, canonicalName)
        if (bundle != null) {
            intent.putExtra(com.will.mvvm.base.ContainerActivity.BUNDLE, bundle)
        }
        startActivity(intent)
    }

    /**
     * =====================================================================
     */
    //刷新布局
    fun refreshLayout() {
        if (::binding.isInitialized) {
            binding.setVariable(viewModelId, viewModel)
        }
    }

    override fun initParam() {}

    /**
     * 初始化根布局
     *
     * @return 布局layout的id
     */
    abstract fun initContentView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): Int

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
    val isBackPressed: Boolean
        get() = false

    /**
     * 创建ViewModel
     *
     * @param cls
     * @param <T>
     * @return
    </T> */
    fun <T : ViewModel?> createViewModel(fragment: Fragment, cls: Class<T>): T {
        return ViewModelProvider(fragment,ViewModelProvider.AndroidViewModelFactory(requireActivity().application)).get(cls)
    }
}