package com.example.appfireflyiii.data.network

import com.example.appfireflyiii.data.model.AccountResponse
import com.example.appfireflyiii.data.model.TransactionResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface FireflyApi {
    @GET("api/v1/accounts")
    suspend fun getAccounts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): AccountResponse

    @GET("api/v1/about")
    suspend fun getAbout(): Map<String, Any>

    @GET("api/v1/transactions")
    suspend fun getTransactions(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
        @Query("type") type: String = "all" // "withdrawal", "deposit", "transfer", "all"
    ): TransactionResponse
}