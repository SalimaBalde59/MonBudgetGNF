package com.orangedigitalcenter.monbudget.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.orangedigitalcenter.monbudget.data.local.entity.Transaction
import com.orangedigitalcenter.monbudget.data.local.entity.TypeTransaction
import com.orangedigitalcenter.monbudget.domain.model.TotalParCategorie
import com.orangedigitalcenter.monbudget.domain.model.TransactionAvecCategorie
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    /** Transactions d'une période, filtrables par catégorie (categorieId = null => toutes). */
    @androidx.room.Transaction
    @Query(
        """
        SELECT * FROM transactions
        WHERE date BETWEEN :debut AND :fin
          AND (:categorieId IS NULL OR categorieId = :categorieId)
        ORDER BY date DESC, id DESC
        """
    )
    fun getEntre(debut: Long, fin: Long, categorieId: Long?): Flow<List<TransactionAvecCategorie>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): Transaction?

    /** Somme des montants d'un type (revenu ou dépense) sur une période. */
    @Query(
        """
        SELECT COALESCE(SUM(montantGnf), 0) FROM transactions
        WHERE type = :type AND date BETWEEN :debut AND :fin
        """
    )
    fun getTotal(type: TypeTransaction, debut: Long, fin: Long): Flow<Long>

    /** Total par catégorie, du plus grand au plus petit (pour le graphique). */
    @Query(
        """
        SELECT c.id AS categorieId, c.nom AS nom, c.couleur AS couleur, SUM(t.montantGnf) AS total
        FROM transactions t
        INNER JOIN categories c ON c.id = t.categorieId
        WHERE t.type = :type AND t.date BETWEEN :debut AND :fin
        GROUP BY c.id
        ORDER BY total DESC
        """
    )
    fun getRepartition(type: TypeTransaction, debut: Long, fin: Long): Flow<List<TotalParCategorie>>

    @Insert
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)
}
