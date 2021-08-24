package com.will.mvvm.binding.viewadapter.recyclerview

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemAnimator
import com.will.mvvm.binding.command.BindingCommand
import java.util.concurrent.TimeUnit

/**
 * @author will
 */
object ViewAdapter {
    @BindingAdapter("lineManager")
    fun setLineManager(recyclerView: RecyclerView, lineManagerFactory: LineManagers.LineManagerFactory) {
        recyclerView.addItemDecoration(lineManagerFactory.create(recyclerView))
    }

    //    @BindingAdapter("layoutManager")
    //    public static void setLayoutManager(RecyclerView recyclerView, LayoutManagers.LayoutManagerFactory layoutManagerFactory) {
    //        recyclerView.setLayoutManager(layoutManagerFactory.create(recyclerView));
    //    }
    @BindingAdapter(value = ["onScrollChangeCommand", "onScrollStateChangedCommand"], requireAll = false)
    fun onScrollChangeCommand(recyclerView: RecyclerView,
                              onScrollChangeCommand: BindingCommand<ScrollDataWrapper?>?,
                              onScrollStateChangedCommand: BindingCommand<Int?>?) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private var state = 0
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                onScrollChangeCommand?.execute(ScrollDataWrapper(dx.toFloat(), dy.toFloat(), state))
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                state = newState
                onScrollStateChangedCommand?.execute(newState)
            }
        })
    }

    @BindingAdapter("onLoadMoreCommand")
    fun onLoadMoreCommand(recyclerView: RecyclerView, onLoadMoreCommand: BindingCommand<Int?>) {
        val listener: RecyclerView.OnScrollListener = OnScrollListener(onLoadMoreCommand)
        recyclerView.addOnScrollListener(listener)
    }

    @BindingAdapter("itemAnimator")
    fun setItemAnimator(recyclerView: RecyclerView, animator: ItemAnimator?) {
        recyclerView.itemAnimator = animator
    }

    class OnScrollListener(onLoadMoreCommand: BindingCommand<Int?>) : RecyclerView.OnScrollListener() {
        private val onLoadMoreCommand: BindingCommand<Int?>?
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
            val visibleItemCount = layoutManager!!.childCount
            val totalItemCount = layoutManager.itemCount
            val pastVisiblesItems = layoutManager.findFirstVisibleItemPosition()
            if (visibleItemCount + pastVisiblesItems >= totalItemCount) {
                if (onLoadMoreCommand != null) {
                }
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
        }

        init {
            this.onLoadMoreCommand = onLoadMoreCommand
        }
    }

    class ScrollDataWrapper(var scrollX: Float, var scrollY: Float, var state: Int)
}