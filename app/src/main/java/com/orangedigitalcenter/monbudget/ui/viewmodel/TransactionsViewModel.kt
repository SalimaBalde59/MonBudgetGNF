package com.orangedigitalcenter.monbudget.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orangedigitalcenter.monbudget.data.local.entity.Categorie
import com.orangedigitalcenter.monbudget.data.local.entity.Transaction
import com.orangedigitalcenter.monbudget.domain.model.TransactionAvecCategorie
import com.orangedigitalcenter.monbudget.domain.repository.CategorieRepository
import com.orangedigitalcenter.monbudget.domain.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth

data class TransactionsUiState(
    val mois: YearMonth,
    val categorieFiltreId: Long? = null,
    val categories: List<Categorie> = emptyList(),
    val transactions: List<TransactionAvecCategorie> = emptyList(),
    val chargement: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsViewModel(
    private val transactions: TransactionRepository,
    categories: CategorieRepository,
    moisInitial: YearMonth = YearMonth.now()
) : ViewModel() {

    private val mois = MutableStateFlow(moisInitial)
    private val filtre = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<TransactionsUiState> = combine(mois, filtre) { m, f -> m to f }
        .flatMapLatest { (m, f) ->
            combine(transactions.getTransactions(m, f), categories.getAll()) { liste, cats ->
                TransactionsUiState(
                    mois = m,
                    categorieFiltreId = f,
                    categories = cats,
                    transactions = liste,
                    chargement = false
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransactionsUiState(moisInitial))

    fun moisPrecedent() { mois.value = mois.value.minusMonths(1) }
    fun moisSuivant() { mois.value = mois.value.plusMonths(1) }

    /** categorieId = null => toutes les catégories. */
    fun filtrerParCategorie(categorieId: Long?) { filtre.value = categorieId }

    fun supprimer(transaction: Transaction) {
        viewModelScope.launch { transactions.supprimer(transaction) }
    }
}
