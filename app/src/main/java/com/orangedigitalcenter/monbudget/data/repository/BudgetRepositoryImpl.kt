package com.orangedigitalcenter.monbudget.data.repository

import com.orangedigitalcenter.monbudget.data.local.dao.BudgetDao
import com.orangedigitalcenter.monbudget.data.local.dao.CategorieDao
import com.orangedigitalcenter.monbudget.data.local.dao.TransactionDao
import com.orangedigitalcenter.monbudget.data.local.entity.BudgetMensuel
import com.orangedigitalcenter.monbudget.data.local.entity.TypeTransaction
import com.orangedigitalcenter.monbudget.domain.model.BudgetAvecSuivi
import com.orangedigitalcenter.monbudget.domain.model.calculerPourcentage
import com.orangedigitalcenter.monbudget.domain.model.cle
import com.orangedigitalcenter.monbudget.domain.model.debutMillis
import com.orangedigitalcenter.monbudget.domain.model.finMillis
import com.orangedigitalcenter.monbudget.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.YearMonth

class BudgetRepositoryImpl(
    private val budgetDao: BudgetDao,
    private val categorieDao: CategorieDao,
    private val transactionDao: TransactionDao
) : BudgetRepository {

    override fun getBudgetsAvecSuivi(mois: YearMonth): Flow<List<BudgetAvecSuivi>> =
        combine(
            budgetDao.getByMois(mois.cle()),
            categorieDao.getAll(),
            transactionDao.getRepartition(TypeTransaction.DEPENSE, mois.debutMillis(), mois.finMillis())
        ) { budgets, categories, repartition ->
            val categoriesParId = categories.associateBy { it.id }
            val depenseParCategorie = repartition.associate { it.categorieId to it.total }
            budgets.mapNotNull { budget ->
                val categorie = categoriesParId[budget.categorieId] ?: return@mapNotNull null
                val depense = depenseParCategorie[budget.categorieId] ?: 0L
                BudgetAvecSuivi(
                    budget = budget,
                    categorie = categorie,
                    depenseGnf = depense,
                    restantGnf = budget.plafondGnf - depense,
                    pourcentage = calculerPourcentage(depense, budget.plafondGnf, max = 999),
                    depasse = depense > budget.plafondGnf
                )
            }
        }

    override suspend fun definirBudget(categorieId: Long, mois: YearMonth, plafondGnf: Long) {
        require(plafondGnf > 0) { "Le plafond doit être supérieur à 0." }
        budgetDao.upsert(BudgetMensuel(categorieId = categorieId, mois = mois.cle(), plafondGnf = plafondGnf))
    }

    override suspend fun supprimerBudget(budgetId: Long) = budgetDao.supprimer(budgetId)
}
