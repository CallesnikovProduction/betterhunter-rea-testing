package dev.betterhunter.app.di

import dev.betterhunter.salary.SalaryLabels
import dev.betterhunter.search.engine.*
import dev.betterhunter.search.model.*
import dev.betterhunter.app.integration.VacancySalaryFormatterAdapter
import dev.betterhunter.app.integration.VacancyToSearchDocumentAdapter
import dev.betterhunter.vacancy.data.mock.MockVacancyRepository
import dev.betterhunter.vacancy.domain.model.*
import dev.betterhunter.vacancy.presentation.mapper.VacancyUiMapper
import dev.betterhunter.vacancy.presentation.model.SalaryTextFormatter
import dev.betterhunter.vacancy.presentation.model.VacancyPresentationLabels
import dev.betterhunter.vacancy.presentation.model.VacancyScreenLabels
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppGraph {
    val vacancies = MockVacancyRepository()
    val filterEngine = SearchFilterEngine()
    private val salaryLabels = object : SalaryLabels {
        override fun salaryFrom() = "от"
        override fun salaryTo() = "до"
        override fun salaryNotSpecified() = "Зарплата не указана"
        override fun viewVacancyInfoSalaryNotSpecified() = "Зарплата не указана"
    }
    val vacancyMapper = VacancyUiMapper(AppVacancyLabels, VacancySalaryFormatterAdapter(salaryLabels))
    val documents = MockVacancyRepository.fixtures.mapIndexed { index, vacancy ->
        VacancyToSearchDocumentAdapter().adapt(vacancy).copy(
            // Fixed synthetic publication dates keep the demo's date sort deterministic.
            publishedAtEpochMillis = 1_792_368_000_000L - index * 86_400_000L,
        )
    }
    private val mutableScenario = MutableStateFlow(MockSearchScenario.SUCCESS)
    val scenario = mutableScenario.asStateFlow()
    fun setScenario(value: MockSearchScenario) { mutableScenario.value = value }
    val gateway: SearchGateway = SearchGateway { request -> MockSearchGateway(documents, mutableScenario.value).search(request) }
}

object AppVacancyLabels : VacancyPresentationLabels, VacancyScreenLabels {
    override fun experience(value: WorkExperience) = when (value) {
        WorkExperience.NO_EXPERIENCE -> "Без опыта"
        WorkExperience.ONE_TO_THREE_YEARS -> "1–3 года"
        WorkExperience.THREE_TO_SIX_YEARS -> "3–6 лет"
        WorkExperience.MORE_THAN_SIX_YEARS -> "Более 6 лет"
        WorkExperience.UNKNOWN -> "Опыт не указан"
    }
    override fun employment(value: EmploymentType) = when (value) {
        EmploymentType.FULL_TIME -> "Полная занятость"
        EmploymentType.PART_TIME -> "Подработка"
        EmploymentType.PROJECT -> "Проектная работа"
        EmploymentType.INTERNSHIP -> "Стажировка"
        EmploymentType.VOLUNTEER -> "Волонтёрство"
        EmploymentType.UNKNOWN -> "Занятость не указана"
    }
    override fun workFormat(value: WorkFormat) = when (value) { WorkFormat.ON_SITE -> "В офисе"; WorkFormat.HYBRID -> "Гибрид"; WorkFormat.REMOTE -> "Удалённо"; WorkFormat.UNKNOWN -> "Формат не указан" }
    override fun schedule(value: WorkSchedule) = when (value) {
        WorkSchedule.FIVE_TWO -> "5 / 2"
        WorkSchedule.SIX_ONE -> "6 / 1"
        WorkSchedule.FLEXIBLE -> "Гибкий график"
        WorkSchedule.SHIFT -> "Сменный график"
        WorkSchedule.ROTATIONAL -> "Вахта"
        WorkSchedule.UNKNOWN -> "График не указан"
    }
    override fun notSpecified() = "Не указано"
    override fun notFound() = "Вакансия не найдена"
    override fun retry() = "Повторить"
    override fun description() = "Описание"
    override fun requirements() = "Требования"
    override fun skills() = "Навыки"
    override fun conditions() = "Условия"
}
