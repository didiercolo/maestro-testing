package com.example.stackdemo.data.repository

import com.example.stackdemo.data.local.JsonDataSource
import com.example.stackdemo.data.model.Product

class ProductRepository(private val source: JsonDataSource) {

    private val cache: List<Product> by lazy { source.loadProducts() }

    fun getAll(): List<Product> = cache

    fun getByVendor(vendor: String): List<Product> =
        cache.filter { it.vendor == vendor }

    fun getById(id: Int): Product? = cache.firstOrNull { it.id == id }

    fun getVendors(): List<String> = cache.map { it.vendor }.distinct()
}
