package com.orangedigitalcenter.monbudget.domain

import com.orangedigitalcenter.monbudget.data.local.entity.Categorie
import com.orangedigitalcenter.monbudget.data.local.entity.TypeTransaction
import com.orangedigitalcenter.monbudget.domain.model.calculerPourcentage
import com.orangedigitalcenter.monbudget.domain.model.debutMillis
import com.orangedigitalcenter.monbudget.domain.model.finMillis
import com.orangedigitalcenter.monbudget.domain.rules.SaisieRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth
import java.time.ZoneId

class SaisieRulesTest {
    private val transport = Categorie(1, "Transport", TypeTransaction.DEPENSE, "#F57C00")

    @Test fun parseMontant_accepte_les_espaces() {
        assertEquals(150_000L, SaisieRules.parseMontant("150 000"))
        assertEquals(150_000L, SaisieRules.parseMontant("150000"))
    }

    @Test fun parseMontant_refuse_le_texte_et_les_negatifs() {
        assertNull(SaisieRules.parseMontant(""))
        assertNull(SaisieRules.parseMontant("abc"))
        assertNull(SaisieRules.parseMontant("-500"))
        assertNull(SaisieRules.parseMontant("12,5"))
    }

    @Test fun erreurMontant_refuse_zero() {
        assertNotNull(SaisieRules.erreurMontant("0"))
        assertNull(SaisieRules.erreurMontant("5000"))
    }

    @Test fun transaction_valide() {
        assertTrue(SaisieRules.validerTransaction("5000", transport, TypeTransaction.DEPENSE).estValide)
    }

    @Test fun transaction_sans_categorie_est_refusee() {
        val e = SaisieRules.validerTransaction("5000", null, TypeTransaction.DEPENSE)
        assertFalse(e.estValide)
        assertNotNull(e.categorie)
        assertNull(e.montant)
    }

    @Test fun categorie_de_mauvais_type_est_refusee() {
        val e = SaisieRules.validerTransaction("5000", transport, TypeTransaction.REVENU)
        assertNotNull(e.categorie)
    }

    @Test fun objectif_exige_un_nom_et_une_cible() {
        assertNotNull(SaisieRules.erreurObjectif("", "100000"))
        assertNotNull(SaisieRules.erreurObjectif("Moto", "0"))
        assertNull(SaisieRules.erreurObjectif("Moto", "1000000"))
    }

    @Test fun pourcentage_protege_contre_zero_et_plafonne() {
        assertEquals(0, calculerPourcentage(100, 0, 100))
        assertEquals(60, calculerPourcentage(60_000, 100_000, 999))
        assertEquals(100, calculerPourcentage(500, 100, 100))
        assertEquals(130, calculerPourcentage(130_000, 100_000, 999))
    }

    @Test fun bornes_du_mois() {
        val utc = ZoneId.of("UTC")
        val octobre = YearMonth.of(2026, 10)
        val unJour = 24L * 3600 * 1000
        assertEquals(31 * unJour - 1, octobre.finMillis(utc) - octobre.debutMillis(utc))
        assertEquals(octobre.debutMillis(utc) + 31 * unJour, YearMonth.of(2026, 11).debutMillis(utc))
    }
}
