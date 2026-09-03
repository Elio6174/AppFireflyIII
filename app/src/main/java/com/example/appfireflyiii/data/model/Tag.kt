package com.example.appfireflyiii.data.model

data class TagResponse(
    val data: List<TagData>
)

data class TagData(
    val type: String,
    val id: String,
    val attributes: TagAttributes
)

data class TagAttributes(
    val tag: String
)