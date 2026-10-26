package com.orangedigitalcenter.monbudget.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.orangedigitalcenter.monbudget.data.local.dao.BudgetDao
import com.orangedigitalcenter.monbudget.data.local.dao.CategorieDao
import com.orangedigitalcenter.monbudget.data.local.dao.ObjectifDao
import com.orangedigitalcenter.monbudget.data.local.dao.TransactionDao
import com.orangedigitalcenter.monbudget.data.local.entity.BudgetMensuel
import com.orangedigitalcenter.monbudget.data.local.entity.Categorie
import com.orangedigitalcenter.monbudget.data.local.entity.ObjectifEpargne
import com.orangedigitalcenter.monbudget.data.local.entity.Transaction
import com.orangedigitalcenter.monbudget.data.local.entity.TypeTransaction

@Database(
    entities = [Categorie::class, Transaction::class, BudgetMensuel::class, ObjectifEpargne::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categorieDao(): CategorieDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun objectifDao(): ObjectifDao

    companion object {
        private const val NOM_BASE = "mon_budget.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    NOM_BASE
                )
                    .addCallback(PrechargementCategories)
                    .build()
                    .also { instance = it }
            }
    }
}

/** Catégories adaptées au contexte guinéen, insérées une seule fois à la création de la base. */
object CategoriesParDefaut {
    val liste: List<Categorie> = listOf(
        // Dépenses
        Categorie(nom = "Transport", type = TypeTransaction.DEPENSE, couleur = "#F57C00"),
        Categorie(nom = "Nourriture", type = TypeTransaction.DEPENSE, couleur = "#43A047"),
        Categorie(nom = "Transfert à la famille", type = TypeTransaction.DEPENSE, couleur = "#8E24AA"),
        Categorie(nom = "Tontine", type = TypeTransaction.DEPENSE, couleur = "#3949AB"),
        Categorie(nom = "Frais Mobile Money", type = TypeTransaction.DEPENSE, couleur = "#FF6F00"),
        Categorie(nom = "Santé", type = TypeTransaction.DEPENSE, couleur = "#E53935"),
        Categorie(nom = "Scolarité", type = TypeTransaction.DEPENSE, couleur = "#1E88E5"),
        Categorie(nom = "Autres dépenses", type = TypeTransaction.DEPENSE, couleur = "#757575"),
        // Revenus
        Categorie(nom = "Salaire", type = TypeTransaction.REVENU, couleur = "#2E7D32"),
        Categorie(nom = "Commerce", type = TypeTransaction.REVENU, couleur = "#00897B"),
        Categorie(nom = "Aide familiale", type = TypeTransaction.REVENU, couleur = "#6D4C41"),
        Categorie(nom = "Autres revenus", type = TypeTransaction.REVENU, couleur = "#546E7A")
    )
}

private object PrechargementCategories : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CategoriesParDefaut.liste.forEach { c ->
            db.execSQL(
                "INSERT INTO categories (nom, type, couleur) VALUES (?, ?, ?)",
                arrayOf<Any?>(c.nom, c.type.name, c.couleur)
            )
        }
    }
}
