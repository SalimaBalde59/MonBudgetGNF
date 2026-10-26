package com.orangedigitalcenter.monbudget.data.repository

import com.orangedigitalcenter.monbudget.data.local.dao.TransactionDao
import com.orangedigitalcenter.monbudget.data.local.entity.Transaction
import com.orangedigitalcenter.monbudget.data.local.entity.TypeTransaction
import com.orangedigitalcenter.monbudget.domain.model.BilanMensuel
import com.orangedigitalcenter.monbudget.domain.model.TransactionAvecCategorie
import com.orangedigitalcenter.monbudget.domain.model.debutMillis
import com.orangedigitalcenter.monbudget.domain.model.finMillis
import com.orangedigitalcenter.monbudget.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.YearMonth

class TransactionRepositoryImpl(private val dao: TransactionDao) : TransactionRepository {

    override fun getTransactions(mois: YearMonth, categorieId: Long?): Flow<List<TransactionAvecCategorie>> =
        dao.getEntre(mois.debutMillis(), mois.finMillis(), categorieId)

    override fun getBilan(mois: YearMonth): Flow<BilanMensuel> {
        val debut = mois.debutMillis()
        val fin = mois.finMillis()
        return combine(
            dao.getTotal(TypeTransaction.REVENU, debut, fin),
            dao.getTotal(TypeTransaction.DEPENSE, debut, fin),
            dao.getRepartition(TypeTransaction.DEPENSE, debut, fin)
        ) { revenus, depenses, repartition ->
            BilanMensuel(
                mois = mois,
                totalRevenus = revenus,
                totalDepenses = depenses,
                solde = revenus - depenses,
                repartitionDepenses = repartition
            )
        }
    }

    override suspend fun ajouter(transaction: Transaction): Long {
        require(transaction.montantGnf > 0) { "Le montant doit être supérieur à 0." }
        return dao.insert(transaction)
    }

    override suspend fun modifier(transaction: Transaction) {
        require(transaction.montantGnf > 0) { "Le montant doit être supérieur à 0." }
        dao.update(transaction)
    }

    override suspend fun supprimer(transaction: Transaction) = dao.delete(transaction)
}
