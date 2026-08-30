package com.example.appfireflyiii.data.network

import com.example.appfireflyiii.data.model.AccountResponse
import com.example.appfireflyiii.data.model.AccountSingleResponse
import com.example.appfireflyiii.data.model.AccountStoreRequest
import com.example.appfireflyiii.data.model.AccountUpdateRequest
import com.example.appfireflyiii.data.model.TransactionResponse
import com.example.appfireflyiii.data.model.TransactionStoreRequest
import com.example.appfireflyiii.data.model.TransactionStoreResponse
import com.example.appfireflyiii.data.model.TransactionUpdateRequest
import com.example.appfireflyiii.data.model.BudgetResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Path
import retrofit2.http.PUT
import retrofit2.http.DELETE

interface FireflyApi {
    @GET("api/v1/accounts")
    suspend fun getAccounts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): AccountResponse

    @GET("api/v1/transactions")
    suspend fun getTransactions(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
        @Query("type") type: String = "all",
        @Query("start") start: String? = null,
        @Query("end") end: String? = null
    ): TransactionResponse

    @POST("api/v1/transactions")
    suspend fun createTransaction(@Body body: TransactionStoreRequest): TransactionStoreResponse

    @GET("api/v1/transactions/{id}")
    suspend fun getTransaction(@Path("id") id: String): TransactionStoreResponse

    @PUT("api/v1/transactions/{id}")
    suspend fun updateTransaction(
        @Path("id") id: String,
        @Body body: TransactionUpdateRequest
    ): TransactionStoreResponse

    @DELETE("api/v1/transactions/{id}")
    suspend fun deleteTransaction(@Path("id") id: String)

    @GET("api/v1/about")
    suspend fun getAbout(): Map<String, Any>

    @GET("api/v1/accounts/{id}/transactions")
    suspend fun getAccountTransactions(
        @Path("id") accountId: String,
        @Query("start") start: String? = null,
        @Query("end") end: String? = null,
        @Query("limit") limit: Int = 100
    ): TransactionResponse

    @PUT("api/v1/accounts/{id}")
    suspend fun updateAccount(
        @Path("id") id: String,
        @Body body: AccountUpdateRequest
    ): AccountSingleResponse

    @DELETE("api/v1/accounts/{id}")
    suspend fun deleteAccount(@Path("id") id: String)

    @POST("api/v1/accounts")
    suspend fun createAccount(@Body body: AccountStoreRequest): AccountSingleResponse

    @GET("api/v1/budgets")
    suspend fun getBudgets(): BudgetResponse
}