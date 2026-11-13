package com.example.stackdemo.ui.productlist

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stackdemo.data.local.JsonDataSource
import com.example.stackdemo.data.repository.ProductRepository

class ProductListViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val source = JsonDataSource(context.applicationContext)
        val repo   = ProductRepository(source)
        @Suppress("UNCHECKED_CAST")
        return ProductListViewModel(repo) as T
    }
}
