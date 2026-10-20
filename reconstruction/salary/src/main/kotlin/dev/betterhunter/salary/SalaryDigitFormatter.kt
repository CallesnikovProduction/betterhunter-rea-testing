package dev.betterhunter.salary

object SalaryDigitFormatter {
    /** Keeps UTF-16 chars recognized by Character.isDigit(char), then groups them by three. */
    @JvmStatic
    fun format(value: String): String {
        val digits = StringBuilder(value.length)
        for (char in value) {
            if (Character.isDigit(char)) digits.append(char)
        }
        if (digits.isEmpty()) return value

        val result = StringBuilder(digits.length + digits.length / 3)
        for (index in digits.indices) {
            if (index > 0 && (digits.length - index) % 3 == 0) result.append(' ')
            result.append(digits[index])
        }
        return result.toString()
    }
}
