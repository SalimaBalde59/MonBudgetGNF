package com.orangedigitalcenter.monbudget.fakes

import com.orangedigitalcenter.monbudget.data.local.entity.Categorie
import com.orangedigitalcenter.monbudget.data.local.entity.ObjectifEpargne
import com.orangedigitalcenter.monbudget.data.local.entity.Transaction
import com.orangedigitalcenter.monbudget.data.local.entity.TypeTransaction
import com.orangedigitalcenter.monbudget.domain.model.BilanMensuel
import com.orangedigitalcenter.monbudget.domain.model.BudgetAvecSuivi
import com.orangedigitalcenter.monbudget.domain.model.ObjectifAvecProgression
import com.orangedigitalcenter.monbudget.domain.model.TransactionAvecCategorie
import com.orangedigitalcenter.monbudget.domain.model.calculerPourcentage
import com.orangedigitalcenter.monbudget.domain.model.debutMillis
import com.orangedigitalcenter.monbudget.domain.model.finMillis
import com.orangedigitalcenter.monbudget.domain.repository.BudgetRepository
import com.orangedigitalcenter.monbudget.domain.repository.CategorieRepository
import com.orangedigitalcenter.monbudget.domain.repository.ObjectifRepository
import com.orangedigitalcenter.monbudget.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.YearMonth

/** Faux Repository en mémoire : permettent de tester les ViewModels sans base Room. */

class FakeCategorieRepository(initial: List<Categorie> = emptyList()) : CategorieRepository {
    val categories = MutableStateFlow(initial)

    override fun getAll(): Flow<List<Categorie>> = categories
    override fun getByType(type: TypeTransaction): Flow<List<Categorie>> =
        categories.map { liste -> liste.filter { it.type == type } }

    override suspend fun ajouter(categorie: Categorie): Long {
        val id = (categories.value.maxOfOrNull { it.id } ?: 0L) + 1
        categories.value = categories.value + categorie.copy(id = id)
        return id
    }

    override suspend fun modifier(categorie: Categorie) {
        categories.value = categories.value.map { if (it.id == categorie.id) categorie else it }
    }

    override suspend fun supprimer(categorie: Categorie) {
        categories.value = categories.value.filter { it.id != categorie.id }
    }
}

class FakeTransactionRepository(private val categories: FakeCategorieRepository) : TransactionRepository {
    val transactions = MutableStateFlow<List<Transaction>>(emptyList())

    private fun duMois(t: Transaction, mois: YearMonth) = t.date in mois.debutMillis()..mois.finMillis()

    override fun getTransactions(mois: YearMonth, categorieId: Long?): Flow<List<TransactionAvecCategorie>> =
        combine(transactions, categories.categories) { ts, cs ->
            ts.filter { duMois(it, mois) && (categorieId == null || it.categorieId == categorieId) }
                .mapNotNull { t -> cs.find { it.id == t.categorieId }?.let { TransactionAvecCategorie(t, it) } }
                .sortedByDescending { it.transaction.date }
        }

    override fun getBilan(mois: YearMonth): Flow<BilanMensuel> = transactions.map { ts ->
        val dumois = ts.filter { duMois(it, mois) }
        val revenus = dumois.filter { it.type == TypeTransaction.REVENU }.sumOf { it.montantGnf }
        val depenses = dumois.filter { it.type == TypeTransaction.DEPENSE }.sumOf { it.montantGnf }
        BilanMensuel(mois, revenus, depenses, revenus - depenses, emptyList())
    }

    override suspend fun ajouter(transaction: Transaction): Long {
        val id = (transactions.value.maxOfOrNull { it.id } ?: 0L) + 1
        transactions.value = transactions.value + transaction.copy(id = id)
        return id
    }

    override suspend fun modifier(transaction: Transaction) {
        transactions.value = transactions.value.map { if (it.id == transaction.id) transaction else it }
    }

    override suspend fun supprimer(transaction: Transaction) {
        transactions.value = transactions.value.filter { it.id != transaction.id }
    }
}

class FakeBudgetRepository : BudgetRepository {
    val budgets = MutableStateFlow<List<BudgetAvecSuivi>>(emptyList())
    val appelsDefinir = mutableListOf<Triple<Long, YearMonth, Long>>()

    override fun getBudgetsAvecSuivi(mois: YearMonth): Flow<List<BudgetAvecSuivi>> = budgets

    override suspend fun definirBudget(categorieId: Long, mois: YearMonth, plafondGnf: Long) {
        appelsDefinir += Triple(categorieId, mois, plafondGnf)
    }

    override suspend fun supprimerBudget(budgetId: Long) {
        budgets.value = budgets.value.filter { it.budget.id != budgetId }
    }
}

class FakeObjectifRepository : ObjectifRepository {
    val objectifs = MutableStateFlow<List<ObjectifEpargne>>(emptyList())

    override fun getAll(): Flow<List<ObjectifAvecProgression>> = objectifs.map { liste ->
        liste.map {
            ObjectifAvecProgression(
                it,
                calculerPourcentage(it.montantActuelGnf, it.cibleGnf, 100),
                it.montantActuelGnf >= it.cibleGnf
            )
        }
    }

    override suspend fun ajouter(objectif: ObjectifEpargne): Long {
        val id = (objectifs.value.maxOfOrNull { it.id } ?: 0L) + 1
        objectifs.value = objectifs.value + objectif.copy(id = id)
        return id
    }

    override suspend fun modifier(objectif: ObjectifEpargne) {
        objectifs.value = objectifs.value.map { if (it.id == objectif.id) objectif else it }
    }

    override suspend fun supprimer(objectif: ObjectifEpargne) {
        objectifs.value = objectifs.value.filter { it.id != objectif.id }
    }

    override suspend fun ajouterVersement(objectifId: Long, montantGnf: Long) {
        objectifs.value = objectifs.value.map {
            if (it.id == objectifId) it.copy(montantActuelGnf = it.montantActuelGnf + montantGnf) else it
        }
    }
}
