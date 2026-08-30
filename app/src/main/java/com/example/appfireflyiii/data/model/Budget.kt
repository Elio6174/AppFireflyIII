package com.example.appfireflyiii.data.model

data class BudgetResponse(
    val data: List<BudgetData>
)

data class BudgetData(
    val id: String,
    val attributes: BudgetAttributes
)

data class BudgetAttributes(
    val name: String,
    val active: Boolean
)