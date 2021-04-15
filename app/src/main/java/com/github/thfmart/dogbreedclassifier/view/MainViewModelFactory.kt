package com.github.thfmart.dogbreedclassifier.view

import android.content.Context
import android.content.res.AssetManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class MainViewModelFactory(private val assets: AssetManager, private val applicationContext: Context) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel(assets, applicationContext) as T
    }

}