package com.example.appfireflyiii.data.model

import com.google.gson.annotations.SerializedName

data class AccountResponse(
    val data: List<AccountData>,
    val meta: Meta,
    val links: ResponseLinks
)

data class AccountData(
    val type: String,
    val id: String,
    val attributes: AccountAttributes
)

data class AccountAttributes(
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    val active: Boolean,
    val order: Int?,
    val name: String,
    val type: String,
    @SerializedName("account_role") val accountRole: String?,
    @SerializedName("currency_code") val currencyCode: String?,
    @SerializedName("currency_symbol") val currencySymbol: String?,
    @SerializedName("currency_decimal_places") val currencyDecimalPlaces: Int?,
    @SerializedName("current_balance") val currentBalance: String,
    @SerializedName("opening_balance") val openingBalance: String?,
    @SerializedName("virtual_balance") val virtualBalance: String?,
    @SerializedName("debt_amount") val debtAmount: String?,
    val notes: String?,
    @SerializedName("liability_type") val liabilityType: String?,
    @SerializedName("liability_direction") val liabilityDirection: String?,
    val interest: String?,
    @SerializedName("interest_period") val interestPeriod: String?,
    @SerializedName("include_net_worth") val includeNetWorth: Boolean?,
    @SerializedName("last_activity") val lastActivity: String?,
    @SerializedName("account_number") val accountNumber: String?
)

data class Meta(
    val pagination: Pagination?
)

data class Pagination(
    val total: Int,
    val count: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("total_pages") val totalPages: Int
)

data class ResponseLinks(
    val self: String?,
    val first: String?,
    val last: String?
)

data class AccountUpdateRequest(
    val name: String,
    @SerializedName("account_number") val accountNumber: String? = null,
    @SerializedName("account_role") val accountRole: String? = null,
    val notes: String? = null,
    val active: Boolean = true,
    @SerializedName("include_net_worth") val includeNetWorth: Boolean = true
)

data class AccountSingleResponse(
    val data: AccountData
)

data class AccountStoreRequest(
    val name: String,
    val type: String = "asset",
    @SerializedName("account_role") val accountRole: String? = "defaultAsset",
    @SerializedName("currency_code") val currencyCode: String? = "MXN",
    @SerializedName("opening_balance") val openingBalance: String? = null,
    @SerializedName("opening_balance_date") val openingBalanceDate: String? = null,
    @SerializedName("account_number") val accountNumber: String? = null,
    val notes: String? = null,
    val active: Boolean = true,
    @SerializedName("include_net_worth") val includeNetWorth: Boolean = true
)