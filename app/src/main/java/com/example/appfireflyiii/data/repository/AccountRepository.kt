package com.example.appfireflyiii.data.repository

import com.example.appfireflyiii.data.model.AccountData
import com.example.appfireflyiii.data.model.AccountStoreRequest
import com.example.appfireflyiii.data.model.AccountUpdateRequest
import com.example.appfireflyiii.data.model.TransactionGroup
import com.example.appfireflyiii.data.network.FireflyApi

class AccountRepository(private val api: FireflyApi) {

    suspend fun getAccounts(): Result<List<AccountData>> {
        return try {
            val assets = api.getAccounts(type = "asset").data
            val liabilities = api.getAccounts(type = "liability").data
            val realAccounts = (assets + liabilities).filter { it.attributes.type != "initial-balance" }
            Result.success(realAccounts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAccountTransactions(
        accountId: String,
        start: String,
        end: String
    ): Result<List<TransactionGroup>> {
        return try {
            val response = api.getAccountTransactions(accountId, start, end)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAccount(accountId: String, request: AccountUpdateRequest): Result<Unit> {
        return try {
            api.updateAccount(accountId, request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAccount(accountId: String): Result<Unit> {
        return try {
            api.deleteAccount(accountId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAccount(request: AccountStoreRequest): Result<Unit> {
        return try {
            api.createAccount(request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}