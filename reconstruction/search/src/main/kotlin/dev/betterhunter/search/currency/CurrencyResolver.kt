package dev.betterhunter.search.currency

/**
 * Resolves an explicit currency code or applies BetterHunter's country-context policy.
 * This interface is intentionally separate from any third-party service or HH behavior.
 */
fun interface CurrencyResolver {
    fun resolve(explicitCode: String?, countryContext: String?): String?
}

/**
 * BetterHunter MVP policy: preserve any non-blank explicit ISO-like code uppercased;
 * otherwise use a small documented country/host mapping. Unknown context returns null.
 */
class DefaultCurrencyResolver(
    private val countryCurrencies: Map<String, String> = mapOf(
        "RU" to "RUB",
        "BY" to "BYN",
        "KZ" to "KZT",
        "AZ" to "AZN",
        "GE" to "GEL",
        "KG" to "KGS",
        "UZ" to "UZS",
        "UA" to "UAH",
        "hh.ru" to "RUB",
        "hh.by" to "BYN",
        "hh.kz" to "KZT",
        "hh.az" to "AZN",
        "hh.uz" to "UZS",
        "hh.ge" to "GEL",
        "hh.kg" to "KGS",
    ),
) : CurrencyResolver {
    override fun resolve(explicitCode: String?, countryContext: String?): String? {
        explicitCode?.trim()?.takeIf(String::isNotEmpty)?.let { return it.uppercase() }
        return countryContext?.trim()?.lowercase()?.let { context ->
            countryCurrencies[context] ?: countryCurrencies[context.uppercase()]
        }
    }
}
