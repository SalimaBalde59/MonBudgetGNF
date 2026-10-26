package com.orangedigitalcenter.monbudget.domain.rules

import com.orangedigitalcenter.monbudget.data.local.entity.Categorie
import com.orangedigitalcenter.monbudget.data.local.entity.TypeTransaction

data class ErreursSaisie(val montant: String? = null, val categorie: String? = null) {
    val estValide: Boolean get() = montant == null && categorie == null
}

/** Règles de validation, sans dépendance Android : testables sur JVM. */
object SaisieRules {

    /** "150 000" -> 150000 ; renvoie null si le texte n'est pas un nombre entier positif. */
    fun parseMontant(texte: String): Long? {
        val net = texte.replace(" ", "").replace("\u00A0", "").replace("\u202F", "")
        return if (net.isNotEmpty() && net.length <= 15 && net.all { it.isDigit() }) net.toLongOrNull() else null
    }

    /** Message d'erreur sur un montant, ou null si valide. */
    fun erreurMontant(texte: String): String? {
        if (texte.isBlank()) return "Saisissez un montant."
        val montant = parseMontant(texte) ?: return "Le montant doit contenir uniquement des chiffres."
        return if (montant <= 0) "Le montant doit être supérieur à 0 GNF." else null
    }

    fun validerTransaction(montantTexte: String, categorie: Categorie?, type: TypeTransaction): ErreursSaisie =
        ErreursSaisie(
            montant = erreurMontant(montantTexte),
            categorie = when {
                categorie == null -> "Choisissez une catégorie."
                categorie.type != type -> "Cette catégorie ne correspond pas au type choisi."
                else -> null
            }
        )

    fun erreurObjectif(nom: String, cibleTexte: String): String? = when {
        nom.isBlank() -> "Donnez un nom à l'objectif."
        erreurMontant(cibleTexte) != null -> erreurMontant(cibleTexte)
        else -> null
    }
}
