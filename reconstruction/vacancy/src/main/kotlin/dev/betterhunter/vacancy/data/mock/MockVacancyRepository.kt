package dev.betterhunter.vacancy.data.mock

import dev.betterhunter.vacancy.domain.model.*

enum class MockScenario { SUCCESS, EMPTY, ERROR, INCOMPLETE, LONG_DESCRIPTION }

sealed interface VacancyLoadResult {
    data class Success(val vacancies: List<Vacancy>) : VacancyLoadResult
    data class Failure(val message: String) : VacancyLoadResult
}

class MockVacancyRepository {
    fun load(scenario: MockScenario = MockScenario.SUCCESS): VacancyLoadResult = when (scenario) {
        MockScenario.SUCCESS -> VacancyLoadResult.Success(fixtures)
        MockScenario.EMPTY -> VacancyLoadResult.Success(emptyList())
        MockScenario.ERROR -> VacancyLoadResult.Failure("Не удалось загрузить вакансии")
        MockScenario.INCOMPLETE -> VacancyLoadResult.Success(listOf(fixtures[15]))
        MockScenario.LONG_DESCRIPTION -> VacancyLoadResult.Success(listOf(fixtures[16]))
    }

    fun find(id: String): Vacancy? = fixtures.firstOrNull { it.id == id }

    companion object {
        private val longDescription = (1..80).joinToString("\n\n") {
            "Команда строит надёжные цифровые сервисы и регулярно улучшает архитектуру, процессы разработки и документацию. Разработчик участвует в проектировании, проверке качества и обмене знаниями. Раздел $it описывает синтетический пример обязанностей без данных реальной компании."
        }

        val fixtures: List<Vacancy> = listOf(
            vacancy("v-001", "Android-разработчик", "Северный ветер", "Москва", WorkExperience.THREE_TO_SIX_YEARS, WorkFormat.HYBRID, 260_000, 340_000, "Kotlin", "Coroutines", "Compose"),
            vacancy("v-002", "Backend-разработчик Kotlin", "Лаборатория данных", "Санкт-Петербург", WorkExperience.ONE_TO_THREE_YEARS, WorkFormat.REMOTE, 180_000, 260_000, "Kotlin", "PostgreSQL", "Kafka"),
            vacancy("v-003", "Стажёр-разработчик", "Тихий код", "Казань", WorkExperience.NO_EXPERIENCE, WorkFormat.ON_SITE, 45_000, 65_000, "Git", "Основы Kotlin"),
            vacancy("v-004", "Инженер по качеству", "Маяк систем", "Екатеринбург", WorkExperience.ONE_TO_THREE_YEARS, WorkFormat.HYBRID, 120_000, null, "Тестирование", "SQL"),
            vacancy("v-005", "Аналитик продукта", "Открытая мастерская", "Новосибирск", WorkExperience.THREE_TO_SIX_YEARS, WorkFormat.REMOTE, null, 210_000, "SQL", "Метрики", "A/B-тесты"),
            vacancy("v-006", "Дизайнер интерфейсов", "Ясный интерфейс", "Москва", WorkExperience.ONE_TO_THREE_YEARS, WorkFormat.HYBRID, 150_000, 220_000, "Figma", "Прототипирование"),
            vacancy("v-007", "Специалист поддержки", "Добрый сервис", "Самара", WorkExperience.NO_EXPERIENCE, WorkFormat.ON_SITE, 70_000, 95_000, "Коммуникация", "CRM"),
            vacancy("v-008", "Инженер данных", "Архив облаков", "Пермь", WorkExperience.THREE_TO_SIX_YEARS, WorkFormat.REMOTE, 240_000, 330_000, "Python", "Airflow", "SQL"),
            vacancy("v-009", "Руководитель команды", "Ровный ритм", "Москва", WorkExperience.MORE_THAN_SIX_YEARS, WorkFormat.HYBRID, 350_000, 480_000, "Лидерство", "Планирование"),
            vacancy("v-010", "Мобильный разработчик", "Лист и точка", "Краснодар", WorkExperience.ONE_TO_THREE_YEARS, WorkFormat.REMOTE, 190_000, 270_000, "Android", "Kotlin", "Compose"),
            vacancy("v-011", "Инженер инфраструктуры", "Надёжный контур", "Тюмень", WorkExperience.THREE_TO_SIX_YEARS, WorkFormat.ON_SITE, 210_000, 300_000, "Linux", "Kubernetes", "Terraform"),
            vacancy("v-012", "Редактор технических текстов", "Понятные вещи", "Воронеж", WorkExperience.ONE_TO_THREE_YEARS, WorkFormat.REMOTE, 90_000, 140_000, "Редактура", "Документация"),
            vacancy("v-013", "Специалист по данным", "Сосновый граф", "Уфа", WorkExperience.UNKNOWN, WorkFormat.UNKNOWN, 0, 0, "Excel", "SQL"),
            vacancy("v-014", "Менеджер проектов", "Медленный понедельник", "Нижний Новгород", WorkExperience.THREE_TO_SIX_YEARS, WorkFormat.HYBRID, 170_000, 240_000, "Agile", "Коммуникация"),
            vacancy("v-015", "Разработчик API", "Простая платформа", "Калининград", WorkExperience.ONE_TO_THREE_YEARS, WorkFormat.REMOTE, 160_000, 230_000, "REST", "Kotlin", "HTTP"),
            Vacancy("v-016", "Помощник команды", salary = null, details = null),
            vacancy("v-017", "Архитектор приложений", "Лаборатория вымышленных систем", "Москва", WorkExperience.MORE_THAN_SIX_YEARS, WorkFormat.REMOTE, 400_000, 550_000, "Архитектура", "Kotlin", "Distributed systems", description = longDescription),
            vacancy("v-018", "Координатор проектов", "Мастерская маршрутов", "Омск", WorkExperience.UNKNOWN, WorkFormat.HYBRID, null, null, "Планирование"),
        ).mapIndexed { index, vacancy ->
            val currency = when (index % 6) {
                2 -> "USD"
                3 -> "EUR"
                4 -> "KZT"
                else -> "RUB"
            }
            val employment = when (index % 5) {
                2 -> EmploymentType.INTERNSHIP
                3 -> EmploymentType.PART_TIME
                4 -> EmploymentType.PROJECT
                else -> EmploymentType.FULL_TIME
            }
            val schedule = when (index % 4) {
                1 -> WorkSchedule.FLEXIBLE
                2 -> WorkSchedule.SHIFT
                3 -> WorkSchedule.ROTATIONAL
                else -> WorkSchedule.FIVE_TWO
            }
            vacancy.copy(
                salary = vacancy.salary?.copy(currencyCode = currency),
                employmentTypes = listOf(employment),
                schedules = listOf(schedule),
            )
        }

        private fun vacancy(
            id: String, title: String, employer: String, city: String,
            experience: WorkExperience, format: WorkFormat,
            lower: Long?, upper: Long?, vararg skills: String,
            description: String = "Помогать команде развивать продукт, обсуждать решения и поддерживать качество результата.",
        ) = Vacancy(
            id = id,
            title = title,
            employer = Employer(employer, verified = id.hashCode() % 2 == 0),
            salary = SalaryRange(lower, upper, "RUB"),
            address = VacancyAddress(city = city),
            experience = experience,
            employmentTypes = listOf(EmploymentType.FULL_TIME),
            schedules = listOf(WorkSchedule.FIVE_TWO),
            workFormat = format,
            details = VacancyDetails(
                description = description,
                requirements = listOf("Опыт командной работы", "Готовность обсуждать технические решения"),
                skills = skills.map { VacancySkill(it) },
                additionalConditions = listOf("Оформление по трудовому договору", "Обучение за счёт команды"),
            ),
        )

    }
}
