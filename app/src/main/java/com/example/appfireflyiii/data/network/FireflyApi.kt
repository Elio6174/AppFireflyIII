package com.example.appfireflyiii.data.network

import com.example.appfireflyiii.data.model.AccountResponse
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
}