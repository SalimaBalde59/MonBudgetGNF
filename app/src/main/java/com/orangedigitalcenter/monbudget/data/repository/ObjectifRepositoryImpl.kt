package com.orangedigitalcenter.monbudget.data.repository

import com.orangedigitalcenter.monbudget.data.local.dao.ObjectifDao
import com.orangedigitalcenter.monbudget.data.local.entity.ObjectifEpargne
import com.orangedigitalcenter.monbudget.domain.model.ObjectifAvecProgression
import com.orangedigitalcenter.monbudget.domain.model.calculerPourcentage
import com.orangedigitalcenter.monbudget.domain.repository.ObjectifRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObjectifRepositoryImpl(private val dao: ObjectifDao) : ObjectifRepository {

    override fun getAll(): Flow<List<ObjectifAvecProgression>> =
        dao.getAll().map { liste ->
            liste.map { o ->
                ObjectifAvecProgression(
                    objectif = o,
                    pourcentage = calculerPourcentage(o.montantActuelGnf, o.cibleGnf, max = 100),
                    atteint = o.cibleGnf > 0 && o.montantActuelGnf >= o.cibleGnf
                )
            }
        }

    override suspend fun ajouter(objectif: ObjectifEpargne): Long {
        require(objectif.nom.isNotBlank()) { "Le nom de l'objectif est obligatoire." }
        require(objectif.cibleGnf > 0) { "La cible doit être supérieure à 0." }
        return dao.insert(objectif)
    }

    override suspend fun modifier(objectif: ObjectifEpargne) {
        require(objectif.cibleGnf > 0) { "La cible doit être supérieure à 0." }
        dao.update(objectif)
    }

    override suspend fun supprimer(objectif: ObjectifEpargne) = dao.delete(objectif)

    override suspend fun ajouterVersement(objectifId: Long, montantGnf: Long) {
        require(montantGnf > 0) { "Le versement doit être supérieur à 0." }
        dao.ajouterMontant(objectifId, montantGnf)
    }
}
