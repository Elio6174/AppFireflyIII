package com.example.appfireflyiii.data.repository

import com.example.appfireflyiii.data.model.AccountData
import com.example.appfireflyiii.data.model.TransactionGroup
import com.example.appfireflyiii.data.network.FireflyApi

class AccountRepository(private val api: FireflyApi) {

    suspend fun getAccounts(): Result<List<AccountData>> {
        return try {
            val response = api.getAccounts()
            val realAccounts = response.data.filter { it.attributes.type != "initial-balance" }
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
}