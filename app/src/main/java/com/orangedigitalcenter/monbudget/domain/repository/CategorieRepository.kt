package com.orangedigitalcenter.monbudget.domain.repository

import com.orangedigitalcenter.monbudget.data.local.entity.Categorie
import com.orangedigitalcenter.monbudget.data.local.entity.TypeTransaction
import kotlinx.coroutines.flow.Flow

interface CategorieRepository {
    fun getAll(): Flow<List<Categorie>>
    fun getByType(type: TypeTransaction): Flow<List<Categorie>>
    suspend fun ajouter(categorie: Categorie): Long
    suspend fun modifier(categorie: Categorie)

    /** Lève IllegalStateException si la catégorie est déjà utilisée par des transactions. */
    suspend fun supprimer(categorie: Categorie)
}
