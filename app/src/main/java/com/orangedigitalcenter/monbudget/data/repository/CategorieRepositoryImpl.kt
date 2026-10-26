package com.orangedigitalcenter.monbudget.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.orangedigitalcenter.monbudget.data.local.dao.CategorieDao
import com.orangedigitalcenter.monbudget.data.local.entity.Categorie
import com.orangedigitalcenter.monbudget.data.local.entity.TypeTransaction
import com.orangedigitalcenter.monbudget.domain.repository.CategorieRepository
import kotlinx.coroutines.flow.Flow

class CategorieRepositoryImpl(private val dao: CategorieDao) : CategorieRepository {

    override fun getAll(): Flow<List<Categorie>> = dao.getAll()

    override fun getByType(type: TypeTransaction): Flow<List<Categorie>> = dao.getByType(type)

    override suspend fun ajouter(categorie: Categorie): Long {
        require(categorie.nom.isNotBlank()) { "Le nom de la catégorie est obligatoire." }
        return dao.insert(categorie)
    }

    override suspend fun modifier(categorie: Categorie) {
        require(categorie.nom.isNotBlank()) { "Le nom de la catégorie est obligatoire." }
        dao.update(categorie)
    }

    override suspend fun supprimer(categorie: Categorie) {
        try {
            dao.delete(categorie)
        } catch (e: SQLiteConstraintException) {
            throw IllegalStateException(
                "Cette catégorie est utilisée par des transactions : elle ne peut pas être supprimée.", e
            )
        }
    }
}
