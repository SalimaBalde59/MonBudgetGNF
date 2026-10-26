package com.orangedigitalcenter.monbudget.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class FormatsTest {
    @Test fun formatGnf_groupe_les_milliers() {
        assertEquals("0 GNF", formatGnf(0))
        assertEquals("950 GNF", formatGnf(950))
        assertEquals("1 500 000 GNF", formatGnf(1_500_000))
    }

    @Test fun formatGnf_gere_les_negatifs() {
        assertEquals("-30 000 GNF", formatGnf(-30_000))
    }

    @Test fun formatMois_est_en_francais() {
        assertEquals("octobre 2026", formatMois(YearMonth.of(2026, 10)))
    }

    @Test fun formatDate_jour_mois_annee() {
        val utc = ZoneId.of("UTC")
        val minuit = LocalDate.of(2026, 10, 6).atStartOfDay(utc).toInstant().toEpochMilli()
        assertEquals("06/10/2026", formatDate(minuit, utc))
    }
}
