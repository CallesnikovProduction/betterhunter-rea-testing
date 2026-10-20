# Findings — Search Domain Research

Дата: 2026-10-19. Объект: raw-source/base.apk, SHA-256 0ef900e1066cf63dd86e9924479bfe9bcc0a2669215984c4ccf97141c7953544. Пакет/версия из имеющейся карты: ru.hh.android 26.38 (11487). Статический JADX Headless 0.7.1 через REA; APK не исполнялся.

## Сводка

- Конвертер: ru.hh.applicant.feature.search_vacancy.core.logic.presentation.converter.VacancySearchStateConverter. Точное имя найдено поиском по DEX, методы подтверждены инспекцией класса. FACT [E1, E2].
- a, b, c различаются чтением одного map-key: a берёт первое значение и заменяет null на пустую строку; b берёт первое значение или возвращает null; c возвращает весь список или Kotlin EmptyList. Каждый удаляет ключ до чтения. FACT [E3, E4, E5].
- d(Uri, d) требует ненулевые аргументы, строит собственную insertion-ordered map из query names и values, затем удаляет распознанные ключи. Переданные URI и исходная модель не модифицируются видимым кодом; локальная map изменяется. FACT [E6].
- Caller в пути миграции: SearchStateMigrationMediator.a(String) создаёт default state, преобразует строку через VacancySearchStateConverter.e и вызывает d. FACT [E7]. Это не доказывает всех runtime callers.
- Неизвлечённые ключи сериализуются в encoded query. n1i.a() исключаются из query; n1i.b() остаются в query, но исключаются из предупреждения unknown. Состав множеств UNKNOWN [E6].
- URI-to-model conversion — клиентское состояние, не wire/API contract. Серверный поиск, фильтрация и ranking из APK не восстанавливаются.

## Evidence register

| ID | Evidence |
|---|---|
| E1 | ev_3b2a8bf107bb0a6cdb6f811a6c39643764b1274fe75ad14d542679f8a1ea571e — class search; точное имя плюс synthetic factory. |
| E2 | ev_f3e44713f7108ac10616e0bf99d2e55e69260a14d1d2d825eec14c0bf10895a2 — методы, поля и сигнатуры класса. |
| E3 | ev_eae0cf84dc7579717d8e4ec52e09e11defdd2697a3f02c6e33f163a54b27e155 — тело a. |
| E4 | ev_165c7b870425dc64f288af61a71fcf5859cb1cd7e09aa9fa445005e0ff9339bd — тело b. |
| E5 | ev_eacf0e8ca9bfb332694f3365a91fa885b6a5ba12c025f16c3232882343bf2a27 — тело c. |
| E6 | ev_466a8268a548022f6c5fedf0f0a7ba55b5122495d8ab1cf07d45efba6527cdff — тело d. |
| E7 | ev_e0b02aae3e84f2baf78aa5373eee463a64f9a6306cd8f598817bb564e1bca431 — SearchStateMigrationMediator.a(String). |
| E8 | ev_0c96b944db85d9c0f3a476516951d1ac0f4ec792e9828e780d0fb0b392de1f4b — incoming static using nodes для d, всего 5; не runtime proof. |
| E9 | ev_8105da00304127efc03e5df71050cb276e7fcd2ede91751e6a360adf5e7b97de — CurrencyRepository.d(String), поиск local currency и default fallback. |

## Evidence limits

JADX body — статическое наблюдение shipped APK. Runtime sequencing, dynamic/reflective calls, Android Uri поведение на malformed/duplicate query и серверные эффекты этим не подтверждены. Account/resume/auth/PII намеренно не исследовались. Resume-related field исключён из спецификации.
