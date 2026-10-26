package com.orangedigitalcenter.monbudget.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orangedigitalcenter.monbudget.domain.model.BilanMensuel
import com.orangedigitalcenter.monbudget.domain.model.BudgetAvecSuivi
import com.orangedigitalcenter.monbudget.domain.repository.BudgetRepository
import com.orangedigitalcenter.monbudget.domain.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth

data class DashboardUiState(
    val mois: YearMonth,
    val chargement: Boolean = true,
    val bilan: BilanMensuel? = null,
    val budgetsDepasses: List<BudgetAvecSuivi> = emptyList()
) {
    val estVide: Boolean
        get() = bilan == null || (bilan.totalRevenus == 0L && bilan.totalDepenses == 0L)
}

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    transactions: TransactionRepository,
    budgets: BudgetRepository,
    moisInitial: YearMonth = YearMonth.now()
) : ViewModel() {

    private val mois = MutableStateFlow(moisInitial)

    val uiState: StateFlow<DashboardUiState> = mois
        .flatMapLatest { m ->
            combine(transactions.getBilan(m), budgets.getBudgetsAvecSuivi(m)) { bilan, suivis ->
                DashboardUiState(
                    mois = m,
                    chargement = false,
                    bilan = bilan,
                    budgetsDepasses = suivis.filter { it.depasse }
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState(moisInitial))

    fun moisPrecedent() { mois.value = mois.value.minusMonths(1) }
    fun moisSuivant() { mois.value = mois.value.plusMonths(1) }
}
