package com.will.habit.binding.command

/**
 * A one-argument action.
 *
 * @param <T> the first argument type
</T> */
interface BindingConsumer<T> {
    fun call(t: T)
}