package com.orangedigitalcenter.monbudget.ui.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.orangedigitalcenter.monbudget.MonBudgetApp

private fun CreationExtras.app(): MonBudgetApp =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MonBudgetApp

/** Fabrique unique des ViewModels : injecte les Repository du conteneur. */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val c = app().container
            DashboardViewModel(c.transactionRepository, c.budgetRepository)
        }
        initializer {
            val c = app().container
            TransactionsViewModel(c.transactionRepository, c.categorieRepository)
        }
        initializer {
            val c = app().container
            TransactionFormViewModel(c.transactionRepository, c.categorieRepository)
        }
        initializer {
            val c = app().container
            BudgetsObjectifsViewModel(c.budgetRepository, c.objectifRepository, c.categorieRepository)
        }
    }
}
