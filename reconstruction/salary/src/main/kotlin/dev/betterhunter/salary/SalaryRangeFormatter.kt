package dev.betterhunter.salary

import java.text.NumberFormat
import java.util.Locale

class SalaryRangeFormatter(private val labels: SalaryLabels) {
    fun format(range: SalaryRange?, mask: Int = 0): String {
        if (range == null) return unspecified(mask)
        val lower = range.lowerBound
        val upper = range.upperBound
        return when {
            lower != null && upper != null && lower == upper -> number(lower)
            lower != null && upper != null -> "${number(lower)} – ${number(upper)}"
            lower != null -> "${labels.salaryFrom()} ${number(lower)}"
            upper != null -> "${labels.salaryTo()} ${number(upper)}"
            else -> unspecified(mask)
        }
    }

    private fun unspecified(mask: Int): String =
        if ((mask and 4) != 0) labels.salaryNotSpecified()
        else labels.viewVacancyInfoSalaryNotSpecified()

    private fun number(value: Long): String =
        NumberFormat.getInstance(Locale("ru", "RU")).format(value)
}
