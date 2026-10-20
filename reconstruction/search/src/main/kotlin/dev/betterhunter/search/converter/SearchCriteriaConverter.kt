package dev.betterhunter.search.converter

import dev.betterhunter.search.currency.CurrencyResolver
import dev.betterhunter.search.model.GeoBounds
import dev.betterhunter.search.model.GeoPoint
import dev.betterhunter.search.model.SalaryFilter
import dev.betterhunter.search.model.SearchInputIssue
import dev.betterhunter.search.model.SearchSort
import dev.betterhunter.search.model.VacancySearchCriteria
import dev.betterhunter.search.parser.SearchQueryParameters
import java.math.BigDecimal

class SearchCriteriaConverter(
    private val currencyResolver: CurrencyResolver,
    private val textNormalizer: SearchTextNormalizer = DefaultSearchTextNormalizer,
) {
    fun convert(query: SearchQueryParameters): VacancySearchCriteria {
        val issues = query.issues.toMutableList()
        val salaryRaw = query.first("salary").orEmpty()
        val salary = salaryRaw.takeIf(String::isNotBlank)?.let { raw ->
            val amount = raw.toBigDecimalOrNull()
            if (amount == null) issues += issue("salary", SearchInputIssue.Reason.INVALID_DECIMAL, raw)
            SalaryFilter(raw, amount)
        }

        val selectedPeriod = query.first("period") ?: query.first("search_period")
        val periodDays = selectedPeriod?.toIntOrNull()
        if (selectedPeriod != null && periodDays == null) {
            issues += issue("period", SearchInputIssue.Reason.INVALID_INTEGER, selectedPeriod)
        }

        val primaryBounds = bounds(query, listOf("top_lat", "left_lng", "bottom_lat", "right_lng"), issues)
        val alternateBounds = bounds(
            query,
            listOf("top_right_lat", "bottom_left_lng", "bottom_left_lat", "top_right_lng"),
            issues,
        )
        val point = coordinatePair(query, "sort_point_lat", "sort_point_lng", issues)
        val labels = query.list("label")
        val currencyCode = if (salary == null) null else currencyResolver.resolve(query.first("currency") ?: query.first("currency_code"), query.first("host"))

        return VacancySearchCriteria(
            text = textNormalizer.normalize(query.first("text").orEmpty()),
            searchFields = query.list("search_field"),
            areas = query.list("area"),
            districts = query.list("district"),
            salary = salary,
            experience = query.list("experience").filterNot { it == "doesNotMatter" },
            employmentForms = query.list("employment_form"),
            education = query.list("education"),
            workScheduleByDays = query.list("work_schedule_by_days"),
            workingHours = query.list("working_hours"),
            workFormats = query.list("work_format"),
            sort = query.first("order_by")?.let(::sort) ?: SearchSort.Unspecified,
            periodDays = periodDays,
            geoBounds = alternateBounds ?: primaryBounds,
            sortPoint = point,
            geohash = query.first("geohash"),
            labels = labels.filterNot { it in SPECIAL_LABELS },
            withSalaryOnly = "with_salary" in labels,
            acceptsLaborContract = "accept_labor_contract" in labels,
            internship = "internship" in labels,
            nightShifts = "night_shifts" in labels,
            acceptsTemporary = query.first("accept_temporary").toBooleanCompat(),
            currencyCode = currencyCode,
            salaryFrequency = query.list("salary_frequency"),
            salaryMode = query.first("salary_mode"),
            employers = query.list("employer_id").ifEmpty {
                query.first("head_employer_id")?.let(::listOf).orEmpty()
            },
            currentEmployerId = query.first("current_employer_id"),
            departments = query.list("department"),
            metro = query.list("metro"),
            professionalRoles = query.list("professional_role"),
            industries = query.list("industry"),
            advertisingPlaceIds = query.list("adv_place_id"),
            vacancyId = query.first("vacancy_id"),
            address = query.first("address"),
            driverLicenseTypes = query.list("driver_license_types"),
            inclusivenessTypes = query.list("inclusiveness_types"),
            polygon = query.first("polygon"),
            residualParameters = query.residualParameters,
            issues = issues,
        )
    }

    // This small allowlist is a BetterHunter design choice; source-client sort ids are not inferred.
    private fun sort(raw: String): SearchSort = if (raw in KNOWN_SORT_IDS) {
        SearchSort.Known(raw)
    } else {
        SearchSort.Unknown(raw)
    }

    private fun bounds(
        query: SearchQueryParameters,
        keys: List<String>,
        issues: MutableList<SearchInputIssue>,
    ): GeoBounds? {
        val raw = keys.map { key -> query.first(key) }
        if (raw.all { it == null }) return null
        if (raw.any { it == null }) {
            issues += issue(keys.joinToString(","), SearchInputIssue.Reason.INVALID_COORDINATE)
            return null
        }
        val values = raw.mapIndexed { index, value -> parseCoordinate(value!!, keys[index], issues) }
        if (values.any { it == null }) return null
        val (top, left, bottom, right) = values.filterNotNull()
        if (top !in -90.0..90.0 || bottom !in -90.0..90.0 ||
            left !in -180.0..180.0 || right !in -180.0..180.0
        ) {
            issues += issue(keys.joinToString(","), SearchInputIssue.Reason.INVALID_GEOGRAPHIC_RANGE)
            return null
        }
        return GeoBounds(top, left, bottom, right)
    }

    private fun coordinatePair(
        query: SearchQueryParameters,
        latitudeKey: String,
        longitudeKey: String,
        issues: MutableList<SearchInputIssue>,
    ): GeoPoint? {
        val latitudeRaw = query.first(latitudeKey)
        val longitudeRaw = query.first(longitudeKey)
        if (latitudeRaw == null && longitudeRaw == null) return null
        if (latitudeRaw == null || longitudeRaw == null) {
            issues += issue("$latitudeKey,$longitudeKey", SearchInputIssue.Reason.INVALID_COORDINATE)
            return null
        }
        val latitude = parseCoordinate(latitudeRaw, latitudeKey, issues) ?: return null
        val longitude = parseCoordinate(longitudeRaw, longitudeKey, issues) ?: return null
        if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) {
            issues += issue("$latitudeKey,$longitudeKey", SearchInputIssue.Reason.INVALID_GEOGRAPHIC_RANGE)
            return null
        }
        return GeoPoint(latitude, longitude)
    }

    private fun parseCoordinate(
        raw: String,
        key: String,
        issues: MutableList<SearchInputIssue>,
    ): Double? {
        val value = raw.toDoubleOrNull()
        if (value == null || !value.isFinite()) {
            issues += issue(key, SearchInputIssue.Reason.INVALID_COORDINATE, raw)
            return null
        }
        return value
    }

    private fun issue(
        key: String,
        reason: SearchInputIssue.Reason,
        raw: String? = null,
    ) = SearchInputIssue(key, reason, raw)

    private fun String?.toBooleanCompat(): Boolean = this.equals("true", ignoreCase = true)

    private fun SearchQueryParameters.first(key: String): String? = values[key]?.firstOrNull()
    private fun SearchQueryParameters.list(key: String): List<String> = values[key].orEmpty()

    private val KNOWN_SORT_IDS = setOf("relevance", "publication_time", "salary_desc", "salary_asc")
    private val SPECIAL_LABELS = setOf("with_salary", "accept_labor_contract", "internship", "night_shifts")
}







