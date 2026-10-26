package com.orangedigitalcenter.monbudget.domain.repository

import com.orangedigitalcenter.monbudget.data.local.entity.ObjectifEpargne
import com.orangedigitalcenter.monbudget.domain.model.ObjectifAvecProgression
import kotlinx.coroutines.flow.Flow

interface ObjectifRepository {
    fun getAll(): Flow<List<ObjectifAvecProgression>>
    suspend fun ajouter(objectif: ObjectifEpargne): Long
    suspend fun modifier(objectif: ObjectifEpargne)
    suspend fun supprimer(objectif: ObjectifEpargne)

    /** Ajoute un versement (montant > 0) à l'épargne déjà réunie. */
    suspend fun ajouterVersement(objectifId: Long, montantGnf: Long)
}
