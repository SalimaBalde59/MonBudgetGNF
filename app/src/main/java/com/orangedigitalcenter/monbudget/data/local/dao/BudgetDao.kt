package com.orangedigitalcenter.monbudget.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.orangedigitalcenter.monbudget.data.local.entity.BudgetMensuel
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets_mensuels WHERE mois = :mois")
    fun getByMois(mois: String): Flow<List<BudgetMensuel>>

    /** Un seul budget par (catégorie, mois) : s'il existe déjà, il est remplacé. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(budget: BudgetMensuel): Long

    @Query("DELETE FROM budgets_mensuels WHERE id = :id")
    suspend fun supprimer(id: Long)
}
