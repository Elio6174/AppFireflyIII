package com.example.appfireflyiii.data.repository

import com.example.appfireflyiii.data.model.TagData
import com.example.appfireflyiii.data.network.FireflyApi

class TagRepository(private val api: FireflyApi) {
    suspend fun getTags(): Result<List<TagData>> {
        return try {
            val response = api.getTags()
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}