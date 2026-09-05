package com.example.appfireflyiii.data.repository

import com.example.appfireflyiii.data.model.CategoryData
import com.example.appfireflyiii.data.network.FireflyApi

class CategoryRepository(private val api: FireflyApi) {
    suspend fun getCategories(): Result<List<CategoryData>> {
        return try {
            val response = api.getCategories()
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}