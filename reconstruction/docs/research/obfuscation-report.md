# Obfuscation report

Дата: 2026-10-19. Артефакт: `raw-source/base.apk`, `ru.hh.android` 26.38 (11487), minSdk 32, targetSdk 36.
SHA-256: `0ef900e1066cf63dd86e9924479bfe9bcc0a2669215984c4ccf97141c7953544`.
REA MCP 6.7.0 / JADX Headless 0.7.1. Только статические наблюдения.

**FACT** — наблюдается в метаданных, теле метода или статических ссылках. **INFERENCE** — интерпретация наблюдений. **UNKNOWN** — не установлено. Статическая связь не доказывает выполнение в production.

## Метод оценки имён

Источник: `ev_b13cc7c1e41fd99efc66992791eb2b33c4f503cc55363271a46eb0429afc7c02`. Это детерминированная диагностическая выборка, а не случайная статистическая выборка APK. Из каждого из трёх отсортированных namespace-срезов выбрано 12 равномерно расположенных строк по индексу floor(i*(N−1)/11), i=0..11. Исключены имена с $ и суффиксом __Factory; inner classes, синтетика и DI factories поэтому недопредставлены.

Критерий: осмысленное простое имя содержит предметные слова/роль; короткое непрозрачное имя вроде f, cc5 — obfuscation-like. Это эвристика имени, не доказательство запуска конкретного обфускатора.

| Срез | N до выборки | Осмысленные | Непрозрачные |
|---|---:|---:|---:|
| ru.hh.applicant.feature.search_vacancy.* | 378 | 9/12 (75%) | 3/12 (25%) |
| ru.hh.shared.core.vacancy.* | 50 | 12/12 (100%) | 0/12 |
| defpackage.* | 18 329 | 1/12 (8,3%) | 11/12 (91,7%) |
| Только объединённая выборка | — | 22/36 (61,1%) | 14/36 (38,9%) |

**Эти проценты нельзя переносить на весь APK.** Срезы взвешены поровну вопреки разным N; ru.hh и defpackage имеют разную сохранность. Прикладное происхождение всех defpackage строк не установлено. Повторяющееся имя defpackage.T имеет 4 строки, поэтому inventory rows не равны уникальным именам.

## Полный состав выборки

### search

- `ru.hh.applicant.feature.search_vacancy.autosearch_vacancies.analytics.AutosearchPositionAnalyticsLogger`
- `ru.hh.applicant.feature.search_vacancy.core.search_engine.analytics.search.SearchVacancyHintAnalytics`
- `ru.hh.applicant.feature.search_vacancy.core.search_engine.domain.exception.AiAssistantWoDialogException`
- `ru.hh.applicant.feature.search_vacancy.core.search_engine.domain.quick_filters.converter.builder.QuickFilterConverter`
- `ru.hh.applicant.feature.search_vacancy.filters.analytics.SearchFiltersAnalytics`
- `ru.hh.applicant.feature.search_vacancy.filters.presentation.converter.IndustryUiConverter`
- `ru.hh.applicant.feature.search_vacancy.filters_dialogs.facade.select.employment_form.ui.f`
- `ru.hh.applicant.feature.search_vacancy.filters_dialogs.facade.select.working_hours.custom.ui.i`
- `ru.hh.applicant.feature.search_vacancy.full.facade.SearchVacancyApi`
- `ru.hh.applicant.feature.search_vacancy.full.presentation.search.list.converter.result_converter.VacancySearchAdsConverter`
- `ru.hh.applicant.feature.search_vacancy.full.presentation.search.map.component.b`
- `ru.hh.applicant.feature.search_vacancy.trudvsem_alert.presentation.TrudVsemContinueAlertDelegate`

### shared

- `ru.hh.shared.core.vacancy.contacts.data.converter.VacancyContactsGetPhonesApiErrorConverter`
- `ru.hh.shared.core.vacancy.contacts.domain.model.VacancyContactsPhoneNumberType`
- `ru.hh.shared.core.vacancy.contacts.presentation.converter.VacancyContactsPhoneNumberUiConverter`
- `ru.hh.shared.core.vacancy.converter.HiringTextConverter`
- `ru.hh.shared.core.vacancy.converter.VacancyDriverLicenseConverter`
- `ru.hh.shared.core.vacancy.converter.WorkingConditionsCommonTextConverter`
- `ru.hh.shared.core.vacancy.converter.ui_model.VacancyBodyUiModelsConverter`
- `ru.hh.shared.core.vacancy.converter.ui_model.VacancyEmployerHeaderUiModelConverter`
- `ru.hh.shared.core.vacancy.converter.ui_state.VacancyAddressUiStateConverter`
- `ru.hh.shared.core.vacancy.converter.ui_state.header.VacancyHeaderUiStateConverter`
- `ru.hh.shared.core.vacancy.magritte.cell.recommended_vacancies.RecommendedVacanciesSectionType`
- `ru.hh.shared.core.vacancy.view.info.VacancyDescriptionWebViewProvider`

### root

- `defpackage.JobByEducationUiStateConverter`
- `defpackage.cc5`
- `defpackage.eq5`
- `defpackage.h37`
- `defpackage.jfb`
- `defpackage.lsi`
- `defpackage.o61`
- `defpackage.qj7`
- `defpackage.swa`
- `defpackage.v9b`
- `defpackage.xm6`
- `defpackage.zzi`

## Наблюдения по членам классов

FACT: SearchFiltersViewModel сохраняет предметное имя и типы Feature/UiConverter, но поля v..J и методы Q5/Y5/Z5/a6 непрозрачны. Evidence: `ev_b0e46bf0b46574f08464c7220c521677a29a9e7285a4c3db3bce06710f1484ae`.

FACT: RootRouter имеет методы a..g и поля a..d, несмотря на осмысленное имя класса. Evidence: `ev_1bb9a9435a54621f91832beee1768adeb3c31731e1674c17e2772d8df871831f`.

FACT: SalaryConverter, QuickWorkExperienceFilterConverter и VacancySalaryFrequencyConverter в parsed inventory имеют только конструктор. Evidence: `ev_a364e1c0370dbced7d047cdf68f3066c56c242f7c52064b5c1be28508c8f50c2`, `ev_f15b6e16e634e79d7defc1771afc10a93f5b2e49416102c53bc934ff7dea8016`, `ev_926dbc062a32df961cbd128be403c10a15505349730351d58d1db145d0a36a18`. INFERENCE: возможны shrink/inlining/перенос реализации; причина не доказана. Не считать такие классы готовыми алгоритмами лишь по имени.

FACT: factory method с display name createInstance2 и DEX prototype (Ltoothpick/Scope;)Lru/hh/applicant/core/vacancy_network/VacancyTextQueryConverter; возвращён в smali как createInstance. fell_back=true, markers=[java_body_unavailable], source complete. Evidence: `ev_c5b1919602e5f070bd21ce26e8a520ec8cde1d0243518a6de2896403905f4af4`. Это реальный предел Java rendering; не следует подменять DEX идентичность alias именем.

## Пригодность и ограничения Kotlin recovery

- FACT: небольшие SalaryContentConverter.a/b и VacancySearchStateConverter.a/b/c получены как Java, body available, complete, без fallback markers. Evidence перечислены в reconstruction-candidates.md.
- INFERENCE: предметные пакеты, constructor signatures и короткие чистые helper подходят для направленного анализа; большие reducers/Compose bodies требуют значительно больше контекста.
- UNKNOWN: исходные Kotlin-файлы, локальные имена, comments, точный nullability contract, исходные inline/extension границы, erased generics и полный coroutine control flow.
- DefaultConstructorMarker и bit-mask параметры не следует превращать в произвольную Kotlin сигнатуру. Например SalaryContentConverter.b выбирает строковый ресурс по (i & 4), а не просто по произвольно названному boolean.
- Suspend/lambda/Compose compiler lowering и оптимизация меняют форму программы. Decompiled Java — реконструкция представления bytecode, не исходный Kotlin.
- Статические incoming references не покрывают reflection/dynamic loading. Coverage complete у using nodes не означает полноту runtime call graph.
- Mapping-файл обфускации не предоставлен. Назвать инструмент и уровень его настроек (например R8 rules) по этим данным нельзя.

