package com.example.appfireflyiii.data.repository

import com.example.appfireflyiii.data.model.TransactionGroup
import com.example.appfireflyiii.data.network.FireflyApi

class TransactionRepository(private val api: FireflyApi) {

    suspend fun getTransactions(page: Int = 1): Result<List<TransactionGroup>> {
        return try {
            val response = api.getTransactions(page = page)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}