package com.orangedigitalcenter.monbudget.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Une transaction est soit un revenu, soit une dépense. */
enum class TypeTransaction { REVENU, DEPENSE }

/** Mode de paiement : l'argent circule en espèces et en Mobile Money. */
enum class ModePaiement { ESPECES, MOBILE_MONEY }

@Entity(tableName = "categories")
data class Categorie(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nom: String,
    val type: TypeTransaction,
    val couleur: String          // code hexadécimal, ex. "#F57C00"
)

/**
 * Attention au nom : cette classe s'appelle Transaction (comme dans le modèle du sujet).
 * Dans les DAO, l'annotation Room s'écrit @androidx.room.Transaction (nom complet)
 * pour éviter la confusion.
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Categorie::class,
            parentColumns = ["id"],
            childColumns = ["categorieId"],
            onDelete = ForeignKey.RESTRICT   // on ne supprime pas une catégorie utilisée
        )
    ],
    indices = [Index("categorieId"), Index("date")]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categorieId: Long,
    val montantGnf: Long,                // toujours positif ; le sens est donné par "type"
    val type: TypeTransaction,
    val date: Long,                      // timestamp epoch millis
    val modePaiement: ModePaiement,
    val note: String? = null
)

@Entity(
    tableName = "budgets_mensuels",
    foreignKeys = [
        ForeignKey(
            entity = Categorie::class,
            parentColumns = ["id"],
            childColumns = ["categorieId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    // Un seul budget par catégorie et par mois
    indices = [Index(value = ["categorieId", "mois"], unique = true)]
)
data class BudgetMensuel(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categorieId: Long,
    val mois: String,                    // format "yyyy-MM", ex. "2026-10"
    val plafondGnf: Long
)

@Entity(tableName = "objectifs_epargne")
data class ObjectifEpargne(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nom: String,
    val cibleGnf: Long,
    val montantActuelGnf: Long = 0,
    val dateCible: Long? = null          // epoch millis ; null = pas de date limite
)
