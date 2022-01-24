package com.example.dott.application

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel


open class BaseAndroidViewModel(app: Application) : AndroidViewModel(app) {

    fun getString(@StringRes resId: Int): String = getApplication<Application>().getString(resId)

    fun getString(@StringRes resId: Int, vararg formatArgs: Any?): String =
        getApplication<Application>().getString(resId, *formatArgs)
}