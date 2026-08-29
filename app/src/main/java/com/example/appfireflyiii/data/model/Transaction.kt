package com.example.appfireflyiii.data.model

import com.google.gson.annotations.SerializedName

data class TransactionResponse(
    val data: List<TransactionGroup>,
    val meta: Meta,
    val links: ResponseLinks
)

data class TransactionGroup(
    val type: String,
    val id: String,
    val attributes: TransactionGroupAttributes
)

data class TransactionGroupAttributes(
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("group_title") val groupTitle: String?,
    val transactions: List<TransactionSplit>
)

data class TransactionSplit(
    @SerializedName("transaction_journal_id") val journalId: String?,
    val type: String, // "withdrawal", "deposit", "transfer"
    val date: String,
    val amount: String, // viene como String, igual que en Accounts
    val description: String,
    @SerializedName("currency_symbol") val currencySymbol: String?,
    @SerializedName("currency_decimal_places") val currencyDecimalPlaces: Int?,
    @SerializedName("source_id") val sourceId: String?,
    @SerializedName("source_name") val sourceName: String?,
    @SerializedName("destination_id") val destinationId: String?,
    @SerializedName("destination_name") val destinationName: String?,
    @SerializedName("category_id") val categoryId: String?,
    @SerializedName("category_name") val categoryName: String?,
    @SerializedName("budget_id") val budgetId: String?,
    @SerializedName("budget_name") val budgetName: String?,
    val notes: String?
)