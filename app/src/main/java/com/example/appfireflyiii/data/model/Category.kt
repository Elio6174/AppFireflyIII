package com.example.appfireflyiii.data.model

data class CategoryResponse(
    val data: List<CategoryData>
)

data class CategoryData(
    val id: String,
    val attributes: CategoryAttributes
)

data class CategoryAttributes(
    val name: String
)