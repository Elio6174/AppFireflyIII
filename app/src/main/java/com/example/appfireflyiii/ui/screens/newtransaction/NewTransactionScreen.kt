package com.example.appfireflyiii.ui.screens.newtransaction

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.example.appfireflyiii.data.repository.AccountRepository
import com.example.appfireflyiii.data.repository.BudgetRepository

@Composable
fun NewTransactionScreen(
    navController: NavController,
    viewModel: NewTransactionViewModel,
    accountRepository: AccountRepository,
    budgetRepository: BudgetRepository
) {
    val saveState by viewModel.saveState.collectAsState()

    TransactionFormBody(
        title = "Nueva transacción",
        submitLabel = "Guardar",
        initialValues = TransactionFormInitialValues(),
        saveState = saveState,
        accountRepository = accountRepository,
        budgetRepository = budgetRepository,
        onSave = { type, date, amount, description, sourceId, destinationName, sourceName, destinationId,
                   categoryName, budgetName, notes, tags, foreignAmount, foreignCurrencyCode, applyRules, fireWebhooks ->
            viewModel.save(
                type = type,
                date = date,
                amount = amount,
                description = description,
                sourceId = sourceId,
                destinationName = destinationName,
                sourceName = sourceName,
                destinationId = destinationId,
                categoryName = categoryName,
                budgetName = budgetName,
                notes = notes,
                tags = tags,
                foreignAmount = foreignAmount,
                foreignCurrencyCode = foreignCurrencyCode,
                applyRules = applyRules,
                fireWebhooks = fireWebhooks
            )
        },
        onSavedNavigateBack = { navController.popBackStack() }
    )
}