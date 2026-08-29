package com.example.appfireflyiii.data.repository

import com.example.appfireflyiii.data.model.AccountData
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
}