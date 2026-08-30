package com.example.appfireflyiii.data.repository

import com.example.appfireflyiii.data.model.BudgetData
import com.example.appfireflyiii.data.network.FireflyApi

class BudgetRepository(private val api: FireflyApi) {
    suspend fun getBudgets(): Result<List<BudgetData>> {
        return try {
            val response = api.getBudgets()
            Result.success(response.data.filter { it.attributes.active })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}