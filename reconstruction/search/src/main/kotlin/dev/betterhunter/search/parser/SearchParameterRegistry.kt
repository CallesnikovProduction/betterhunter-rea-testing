package dev.betterhunter.search.parser

/**
 * Registry of parameters this module understands. Residual filtering is deliberately
 * injectable because the source client's two private filtering sets were not recovered.
 */
data class SearchParameterRegistry(
    val multivalueKeys: Set<String> = DEFAULT_MULTIVALUE_KEYS,
    val scalarKeys: Set<String> = DEFAULT_SCALAR_KEYS,
    val omittedFromResidual: Set<String> = emptySet(),
    val recognizedButUnmapped: Set<String> = emptySet(),
) {
    val consumedKeys: Set<String> = multivalueKeys + scalarKeys + PERIOD_KEYS + setOf("currency_code", "head_employer_id")

    companion object {
        val DEFAULT_MULTIVALUE_KEYS = setOf(
            "search_field", "area", "district", "employer_id", "department", "label", "metro",
            "salary_frequency", "experience", "professional_role", "industry", "adv_place_id",
            "employment_form", "education", "work_schedule_by_days", "working_hours", "work_format",
            "driver_license_types", "inclusiveness_types",
        )
        val DEFAULT_SCALAR_KEYS = setOf(
            "text", "salary", "currency", "current_employer_id", "salary_mode", "order_by",
            "vacancy_id", "address", "sort_point_lat", "sort_point_lng", "geohash", "top_lat",
            "bottom_lat", "left_lng", "right_lng", "top_right_lat", "top_right_lng",
            "bottom_left_lat", "bottom_left_lng", "accept_temporary",
        )
        val PERIOD_KEYS = setOf("period", "search_period")
    }
}

