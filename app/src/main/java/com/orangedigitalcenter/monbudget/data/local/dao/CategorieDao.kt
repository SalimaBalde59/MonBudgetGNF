package com.orangedigitalcenter.monbudget.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.orangedigitalcenter.monbudget.data.local.entity.Categorie
import com.orangedigitalcenter.monbudget.data.local.entity.TypeTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CategorieDao {
    @Query("SELECT * FROM categories ORDER BY nom")
    fun getAll(): Flow<List<Categorie>>

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY nom")
    fun getByType(type: TypeTransaction): Flow<List<Categorie>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): Categorie?

    @Insert
    suspend fun insert(categorie: Categorie): Long

    @Update
    suspend fun update(categorie: Categorie)

    @Delete
    suspend fun delete(categorie: Categorie)
}
