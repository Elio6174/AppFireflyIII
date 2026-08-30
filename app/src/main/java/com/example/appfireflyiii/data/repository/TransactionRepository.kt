package com.example.appfireflyiii.data.repository

import com.example.appfireflyiii.data.model.TransactionGroup
import com.example.appfireflyiii.data.model.TransactionStoreRequest
import com.example.appfireflyiii.data.model.TransactionUpdateRequest
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

    suspend fun getTransactionsByRange(start: String, end: String): Result<List<TransactionGroup>> {
        return try {
            val response = api.getTransactions(start = start, end = end, limit = 200)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTransaction(request: TransactionStoreRequest): Result<Unit> {
        return try {
            api.createTransaction(request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTransaction(groupId: String): Result<TransactionGroup> {
        return try {
            val response = api.getTransaction(groupId)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTransaction(groupId: String, request: TransactionUpdateRequest): Result<Unit> {
        return try {
            api.updateTransaction(groupId, request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTransaction(groupId: String): Result<Unit> {
        return try {
            api.deleteTransaction(groupId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}