package com.orangedigitalcenter.monbudget.util

import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/** 1500000 -> "1 500 000 GNF" (espace comme séparateur de milliers). */
fun formatGnf(montant: Long): String {
    val signe = if (montant < 0) "-" else ""
    val chiffres = Math.abs(montant).toString()
    val groupes = chiffres.reversed().chunked(3).joinToString(" ").reversed()
    return "$signe$groupes GNF"
}

/** "octobre 2026" */
fun formatMois(mois: YearMonth): String =
    mois.month.getDisplayName(TextStyle.FULL, Locale.FRENCH) + " " + mois.year

/** "06/10/2026" */
fun formatDate(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
    Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
