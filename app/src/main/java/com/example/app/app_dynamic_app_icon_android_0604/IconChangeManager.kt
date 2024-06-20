package com.example.app.app_dynamic_app_icon_android_0604

import android.app.Activity
import android.content.ComponentName
import android.content.pm.ActivityInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle

internal class IconChangeManager(private val activity: Activity) {
    private val packageManager: PackageManager = activity.packageManager
    private val iconAliases: MutableList<Alias> = ArrayList()
    private val initialAlias: Alias
    private val cloneInitialAlias: Alias
    private val currentAlias: Alias
    val isIconChangeActivated: Boolean

    private var isRefreshing = false

    init {
        var initial: Alias? = null
        var cloneInitial: Alias? = null
        var current: Alias? = null
        val isActivated: Boolean

        try {
            val packageInfo = packageInfo
            for (activityInfo in packageInfo.activities) {
                if (isIconChangeActivityAlias(activityInfo)) {
                    val alias =
                        Alias(
                            activityInfo.name,
                            activityInfo.metaData.getString(META_DATA_KEY_ALIAS_TITLE)!!,
                            activityInfo.icon
                        )

                    if (isAliasComponentEnabled(activityInfo)) {
                        if (current == null) {
                            current = alias
                        } else {
                            throw IllegalStateException("Multiple aliases currently enabled")
                        }
                    }

                    if (activityInfo.enabled) {
                        if (initial == null) {
                            initial = alias
                        } else {
                            throw IllegalStateException(
                                "Multiple initial aliases enabled in manifest"
                            )
                        }
                    } else {
                        iconAliases.add(alias)
                    }
                }
            }

            checkNotNull(current) { "No alias currently enabled" }

            checkNotNull(initial) { "No initial alias enabled in manifest" }
            isActivated = (packageManager
                .getComponentEnabledSetting(ComponentName(activity, initial.className))
                    == PackageManager.COMPONENT_ENABLED_STATE_DISABLED)

            for (alias in iconAliases) {
                if (!alias.equals(initial) && alias.iconResId === initial.iconResId) {
                    if (cloneInitial == null) {
                        cloneInitial = alias
                    } else {
                        throw IllegalStateException("Multiple clone initial aliases found")
                    }
                }
            }

            checkNotNull(cloneInitial) { "No clone initial alias found" }
        } catch (e: PackageManager.NameNotFoundException) {
            throw RuntimeException(e)
        }

        initialAlias = initial
        cloneInitialAlias = cloneInitial
        currentAlias = current
        isIconChangeActivated = isActivated
    }

    val aliases: List<Alias>
        get() = iconAliases

    fun getCurrentAlias(): Alias {
        return currentAlias
    }

    fun activateIconChange() {
        if (!isIconChangeActivated) {
            setAliasComponentEnabled(cloneInitialAlias, true)
            setAliasComponentEnabled(initialAlias, false)
            activity.finish()
        }
    }

    fun deactivateIconChange(): Boolean {
        if (isIconChangeActivated) {
            try {
                val packageInfo = packageInfo
                for (activityInfo in packageInfo.activities) {
                    if (isIconChangeActivityAlias(activityInfo)) {
                        packageManager.setComponentEnabledSetting(
                            createComponentName(activityInfo.name),
                            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                            PackageManager.DONT_KILL_APP
                        )
                    }
                }
                refresh()
                return true
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        } else {
            return false
        }
    }

    fun setCurrentAlias(alias: Alias) {
        if (!currentAlias.equals(alias)) {
            setAliasComponentEnabled(alias, true)
            setAliasComponentEnabled(currentAlias, false)
            refresh()
        }
    }

    fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(EXTRA_REFRESHING, isRefreshing)
    }

    fun isIconChangeRefresh(savedInstanceState: Bundle?): Boolean {
        return savedInstanceState != null && savedInstanceState.getBoolean(EXTRA_REFRESHING)
    }

    @get:Throws(PackageManager.NameNotFoundException::class)
    private val packageInfo: PackageInfo
        get() = packageManager.getPackageInfo(
            activity.packageName,
            PackageManager.GET_ACTIVITIES or PackageManager.GET_DISABLED_COMPONENTS or PackageManager.GET_META_DATA
        )

    private fun isIconChangeActivityAlias(activityInfo: ActivityInfo): Boolean {
        return activityInfo.targetActivity != null && activityInfo.metaData != null &&
                activityInfo.metaData.containsKey(META_DATA_KEY_ALIAS_TITLE)
    }

    private fun isAliasComponentEnabled(aliasInfo: ActivityInfo): Boolean {
        val state =
            packageManager.getComponentEnabledSetting(createComponentName(aliasInfo.name))
        if (state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) {
            return aliasInfo.enabled
        }
        return state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    }

    private fun setAliasComponentEnabled(alias: Alias, enabled: Boolean) {
        val newState =
            if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else if (alias.equals(
                    initialAlias
                )
            ) PackageManager.COMPONENT_ENABLED_STATE_DISABLED else PackageManager.COMPONENT_ENABLED_STATE_DEFAULT

        packageManager.setComponentEnabledSetting(
            createComponentName(alias.className),
            newState, PackageManager.DONT_KILL_APP
        )
    }

    private fun createComponentName(className: String): ComponentName {
        return ComponentName(activity.packageName, className)
    }

    private fun refresh() {
        isRefreshing = true
        activity.recreate()
    }

    companion object {
        private val META_DATA_KEY_ALIAS_TITLE: String =  "com.example.app.app_dynamic_app_icon_android_0604.ALIAS_TITLE"
        private val EXTRA_REFRESHING: String =  "com.example.app.app_dynamic_app_icon_android_0604.EXTRA_REFRESHING"
    }
}