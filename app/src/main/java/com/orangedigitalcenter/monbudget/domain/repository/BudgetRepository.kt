package com.orangedigitalcenter.monbudget.domain.repository

import com.orangedigitalcenter.monbudget.domain.model.BudgetAvecSuivi
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface BudgetRepository {
    /** Budgets du mois avec dépense réelle, reste et indicateur de dépassement. */
    fun getBudgetsAvecSuivi(mois: YearMonth): Flow<List<BudgetAvecSuivi>>

    /** Crée ou remplace le budget d'une catégorie pour un mois. */
    suspend fun definirBudget(categorieId: Long, mois: YearMonth, plafondGnf: Long)

    suspend fun supprimerBudget(budgetId: Long)
}
