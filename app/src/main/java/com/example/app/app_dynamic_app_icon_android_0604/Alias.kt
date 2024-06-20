package com.example.app.app_dynamic_app_icon_android_0604

internal class Alias(val className: String, val title: String, val iconResId: Int) {
    val simpleName: String
        get() {
            val index = className.lastIndexOf('.')
            return if (index != -1) className.substring(index + 1) else className
        }

    override fun toString(): String {
        return title
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        return className == (o as Alias).className
    }

    override fun hashCode(): Int {
        return className.hashCode()
    }
}