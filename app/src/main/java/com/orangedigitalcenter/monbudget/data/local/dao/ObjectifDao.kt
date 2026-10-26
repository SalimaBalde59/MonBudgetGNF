package com.orangedigitalcenter.monbudget.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.orangedigitalcenter.monbudget.data.local.entity.ObjectifEpargne
import kotlinx.coroutines.flow.Flow

@Dao
interface ObjectifDao {
    @Query("SELECT * FROM objectifs_epargne ORDER BY id DESC")
    fun getAll(): Flow<List<ObjectifEpargne>>

    @Query("SELECT * FROM objectifs_epargne WHERE id = :id")
    suspend fun getById(id: Long): ObjectifEpargne?

    @Insert
    suspend fun insert(objectif: ObjectifEpargne): Long

    @Update
    suspend fun update(objectif: ObjectifEpargne)

    @Delete
    suspend fun delete(objectif: ObjectifEpargne)

    @Query("UPDATE objectifs_epargne SET montantActuelGnf = montantActuelGnf + :montant WHERE id = :id")
    suspend fun ajouterMontant(id: Long, montant: Long)
}
