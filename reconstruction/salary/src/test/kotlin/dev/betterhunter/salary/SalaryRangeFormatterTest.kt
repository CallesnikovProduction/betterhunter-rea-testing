package dev.betterhunter.salary

import java.text.NumberFormat
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

class SalaryRangeFormatterTest {
    private val labels = object : SalaryLabels {
        override fun salaryFrom() = "FROM"
        override fun salaryTo() = "TO"
        override fun salaryNotSpecified() = "UNSPECIFIED"
        override fun viewVacancyInfoSalaryNotSpecified() = "VACANCY_INFO_UNSPECIFIED"
    }
    private val formatter = SalaryRangeFormatter(labels)
    private fun n(value: Long) = NumberFormat.getInstance(Locale("ru", "RU")).format(value)

    @Test fun nullAndEmptyRangesUseMaskSelectedLabel() {
        listOf<Int>(0, 8).forEach { assertEquals("VACANCY_INFO_UNSPECIFIED", formatter.format(null, it)) }
        listOf<Int>(4, 5, -4).forEach { mask ->
            assertEquals("UNSPECIFIED", formatter.format(SalaryRange(null, null), mask))
        }
        assertEquals("VACANCY_INFO_UNSPECIFIED", formatter.format(SalaryRange(null, null), 0))
    }

    @Test fun equalAndDifferentBounds() {
        assertEquals(n(1234), formatter.format(SalaryRange(1234, 1234)))
        assertEquals("${n(1000)} – ${n(2000)}", formatter.format(SalaryRange(1000, 2000)))
    }

    @Test fun oneSidedAndReverseRangesKeepTheirOrder() {
        assertEquals("FROM ${n(1000)}", formatter.format(SalaryRange(1000, null)))
        assertEquals("TO ${n(2000)}", formatter.format(SalaryRange(null, 2000)))
        assertEquals("${n(2000)} – ${n(1000)}", formatter.format(SalaryRange(2000, 1000)))
    }

    @Test fun handlesZeroNegativeAndLongExtremesUsingRussianLocale() {
        assertEquals("0", formatter.format(SalaryRange(0, 0)))
        assertEquals(n(-1234567), formatter.format(SalaryRange(-1234567, -1234567)))
        assertEquals("${n(Long.MIN_VALUE)} – ${n(Long.MAX_VALUE)}", formatter.format(SalaryRange(Long.MIN_VALUE, Long.MAX_VALUE)))
    }
}
