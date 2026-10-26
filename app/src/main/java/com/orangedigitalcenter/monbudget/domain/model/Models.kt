package com.orangedigitalcenter.monbudget.domain.model

import androidx.room.Embedded
import androidx.room.Relation
import com.orangedigitalcenter.monbudget.data.local.entity.BudgetMensuel
import com.orangedigitalcenter.monbudget.data.local.entity.Categorie
import com.orangedigitalcenter.monbudget.data.local.entity.ObjectifEpargne
import com.orangedigitalcenter.monbudget.data.local.entity.Transaction
import java.time.YearMonth
import java.time.ZoneId

/** Une transaction avec sa catégorie (pour afficher le nom et la couleur dans la liste). */
data class TransactionAvecCategorie(
    @Embedded val transaction: Transaction,
    @Relation(parentColumn = "categorieId", entityColumn = "id")
    val categorie: Categorie
)

/** Total d'une catégorie sur une période (répartition pour le graphique). */
data class TotalParCategorie(
    val categorieId: Long,
    val nom: String,
    val couleur: String,
    val total: Long
)

/** Bilan d'un mois : revenus, dépenses, solde et répartition des dépenses. */
data class BilanMensuel(
    val mois: YearMonth,
    val totalRevenus: Long,
    val totalDepenses: Long,
    val solde: Long,
    val repartitionDepenses: List<TotalParCategorie>
)

/** Un budget avec son suivi : dépense réelle, reste, pourcentage, dépassement. */
data class BudgetAvecSuivi(
    val budget: BudgetMensuel,
    val categorie: Categorie,
    val depenseGnf: Long,
    val restantGnf: Long,        // négatif si le plafond est dépassé
    val pourcentage: Int,        // 0..999, pour la barre de progression
    val depasse: Boolean
)

/** Un objectif d'épargne avec sa progression. */
data class ObjectifAvecProgression(
    val objectif: ObjectifEpargne,
    val pourcentage: Int,        // 0..100
    val atteint: Boolean
)

/** Pourcentage entier, protégé contre la division par zéro. */
fun calculerPourcentage(valeur: Long, cible: Long, max: Int): Int =
    if (cible <= 0L) 0 else ((valeur * 100) / cible).coerceIn(0L, max.toLong()).toInt()

// --- Bornes d'un mois en epoch millis (fuseau de l'appareil) ---

fun YearMonth.debutMillis(zone: ZoneId = ZoneId.systemDefault()): Long =
    atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()

fun YearMonth.finMillis(zone: ZoneId = ZoneId.systemDefault()): Long =
    plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

/** Clé stockée dans BudgetMensuel.mois, au format "yyyy-MM". */
fun YearMonth.cle(): String = toString()
