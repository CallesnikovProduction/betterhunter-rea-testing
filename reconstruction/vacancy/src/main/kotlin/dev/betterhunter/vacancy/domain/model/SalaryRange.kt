package dev.betterhunter.vacancy.domain.model

/** BetterHunter salary bounds; values are not reordered or parsed from display text. */
data class SalaryRange(
    val lowerBound: Long?,
    val upperBound: Long?,
    val currencyCode: String? = null,
)
