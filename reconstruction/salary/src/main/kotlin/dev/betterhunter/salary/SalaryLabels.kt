package dev.betterhunter.salary

/** Supplies application-localized salary labels; original resource text is intentionally unspecified. */
interface SalaryLabels {
    fun salaryFrom(): String
    fun salaryTo(): String
    fun salaryNotSpecified(): String
    fun viewVacancyInfoSalaryNotSpecified(): String
}
