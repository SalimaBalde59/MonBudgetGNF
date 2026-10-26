package com.orangedigitalcenter.monbudget.domain.repository

import com.orangedigitalcenter.monbudget.data.local.entity.Transaction
import com.orangedigitalcenter.monbudget.domain.model.BilanMensuel
import com.orangedigitalcenter.monbudget.domain.model.TransactionAvecCategorie
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface TransactionRepository {
    /** Transactions d'un mois, filtrables par catégorie (null = toutes les catégories). */
    fun getTransactions(mois: YearMonth, categorieId: Long? = null): Flow<List<TransactionAvecCategorie>>

    /** Revenus, dépenses, solde et répartition des dépenses du mois. */
    fun getBilan(mois: YearMonth): Flow<BilanMensuel>

    suspend fun ajouter(transaction: Transaction): Long
    suspend fun modifier(transaction: Transaction)
    suspend fun supprimer(transaction: Transaction)
}
