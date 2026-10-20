package dev.betterhunter.salary

import kotlin.test.Test
import kotlin.test.assertEquals

class SalaryDigitFormatterTest {
    @Test fun emptyAndNoDigitsArePreserved() {
        assertEquals("", SalaryDigitFormatter.format(""))
        assertEquals("abc -+.,\u00a0", SalaryDigitFormatter.format("abc -+.,\u00a0"))
    }

    @Test fun groupsAsciiDigitsFromTheRight() {
        mapOf(
            "1" to "1", "123" to "123", "1234" to "1 234",
            "123456" to "123 456", "1234567" to "1 234 567",
            "0001234" to "0 001 234",
        ).forEach { (input, expected) -> assertEquals(expected, SalaryDigitFormatter.format(input)) }
    }

    @Test fun stripsSignsSeparatorsAndMixedText() {
        assertEquals("1 234 567", SalaryDigitFormatter.format("-1,234.567 руб."))
    }

    @Test fun supportsBmpUnicodeDigits() {
        assertEquals("١ ٢٣٤", SalaryDigitFormatter.format("١٢٣٤"))
        assertEquals("१२ ३४५", SalaryDigitFormatter.format("१२३४५"))
    }

    @Test fun supplementaryDigitsAreExaminedAsUtf16Chars() {
        val supplementaryDigit = String(Character.toChars(0x1D7D9))
        assertEquals(supplementaryDigit, SalaryDigitFormatter.format(supplementaryDigit))
        assertEquals("1", SalaryDigitFormatter.format("${supplementaryDigit}1"))
    }
}
