package dev.betterhunter.search.model

import java.math.BigDecimal

data class VacancySearchCriteria(
    val text: String = "",
    val searchFields: List<String> = emptyList(),
    val areas: List<String> = emptyList(),
    val districts: List<String> = emptyList(),
    val salary: SalaryFilter? = null,
    val experience: List<String> = emptyList(),
    val employmentForms: List<String> = emptyList(),
    val education: List<String> = emptyList(),
    val workScheduleByDays: List<String> = emptyList(),
    val workingHours: List<String> = emptyList(),
    val workFormats: List<String> = emptyList(),
    val sort: SearchSort = SearchSort.Unspecified,
    /** Null means no finite period in BetterHunter MVP. */
    val periodDays: Int? = null,
    val geoBounds: GeoBounds? = null,
    val sortPoint: GeoPoint? = null,
    val geohash: String? = null,
    val labels: List<String> = emptyList(),
    val withSalaryOnly: Boolean = false,
    val acceptsLaborContract: Boolean = false,
    val internship: Boolean = false,
    val nightShifts: Boolean = false,
    val acceptsTemporary: Boolean = false,
    val currencyCode: String? = null,
    val salaryFrequency: List<String> = emptyList(),
    val salaryMode: String? = null,
    val employers: List<String> = emptyList(),
    val currentEmployerId: String? = null,
    val departments: List<String> = emptyList(),
    val metro: List<String> = emptyList(),
    val professionalRoles: List<String> = emptyList(),
    val industries: List<String> = emptyList(),
    val advertisingPlaceIds: List<String> = emptyList(),
    val vacancyId: String? = null,
    val address: String? = null,
    val driverLicenseTypes: List<String> = emptyList(),
    val inclusivenessTypes: List<String> = emptyList(),
    val polygon: String? = null,
    val residualParameters: Map<String, List<String>> = emptyMap(),
    val issues: List<SearchInputIssue> = emptyList(),
)

data class SalaryFilter(
    val rawAmount: String,
    val amount: BigDecimal?,
)

data class GeoPoint(val latitude: Double, val longitude: Double)

data class GeoBounds(
    val topLatitude: Double,
    val leftLongitude: Double,
    val bottomLatitude: Double,
    val rightLongitude: Double,
)

sealed interface SearchSort {
    data object Unspecified : SearchSort
    data class Known(val id: String) : SearchSort
    data class Unknown(val rawValue: String) : SearchSort
}

data class SearchInputIssue(
    val parameter: String,
    val reason: Reason,
    val rawValue: String? = null,
) {
    enum class Reason {
        MALFORMED_ENCODING,
        INVALID_INTEGER,
        INVALID_DECIMAL,
        INVALID_COORDINATE,
        INVALID_GEOGRAPHIC_RANGE,
        UNKNOWN_PARAMETER,
    }
}


