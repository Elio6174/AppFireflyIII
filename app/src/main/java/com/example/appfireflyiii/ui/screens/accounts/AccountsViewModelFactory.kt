package com.example.appfireflyiii.ui.screens.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appfireflyiii.data.repository.AccountRepository

class AccountsViewModelFactory(
    private val repository: AccountRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AccountsViewModel(repository) as T
    }
}