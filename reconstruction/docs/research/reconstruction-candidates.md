# Reconstruction candidates

Дата: 2026-10-19. Артефакт: `raw-source/base.apk`, `ru.hh.android` 26.38 (11487), minSdk 32, targetSdk 36.
SHA-256: `0ef900e1066cf63dd86e9924479bfe9bcc0a2669215984c4ccf97141c7953544`.
REA MCP 6.7.0 / JADX Headless 0.7.1. Только статические наблюдения.

**FACT** — наблюдается в метаданных, теле метода или статических ссылках. **INFERENCE** — интерпретация наблюдений. **UNKNOWN** — не установлено. Статическая связь не доказывает выполнение в production.

Кандидаты не реализованы; эквивалентность пока не проверялась. Ниже план будущего эксперимента на синтетических данных, без backend, устройства, пользовательской информации и Kotlin-проекта.

## 1. Группировка цифр зарплатной строки — рекомендуемый эксперимент

**Класс:** ru.hh.shared.core.vacancy.converter.SalaryContentConverter, метод a(String):String, overload_index=0.

**FACT:** тело проходит по UTF-16 char, оставляет Character.isDigit; если цифр нет, возвращает исходную строку. Для непустой последовательности применяет m9g.J/B и CollectionsKt.S с размером группы 3 и разделителем обычный пробел. Java body complete, fallback=false. Evidence: `ev_7a7c8c96882c1a9e44b92642d254e453114e5afba8f5e07dec3931c8d94004a1`.

**INFERENCE:** цифры группируются справа по три через reverse/chunk/join/reverse. Точная семантика обфусцированных m9g.J/B ещё UNKNOWN: до реализации нужно исследовать выбранные overloads, а не принимать их за стандартные Kotlin helpers по виду вызова.

**Связь с продуктом:** QuickFiltersReducer.a содержит два прямых INVOKE_STATIC на этот метод, classes6.dex, offsets 859/894. Evidence: `ev_07bc6be3663ded0b8e5494dc46c056536d95e20d6c6e1376da67119514e3fb01`.

**Сложность:** низкая после разрешения двух helper; вход и выход — строки, Android context не нужен. Небольшая CPU-only задача с реальным прикладным caller.

**Проверка эквивалентности:** сначала восстановить точные helpers и сравнить DEX операции с декларативным контрактом; затем независимый reference harness для разрешённого оригинального метода против будущей реализации (это отдельный этап, APK сейчас не запускался). Матрица: пустая строка, строка без цифр, 1/3/4/6/7 цифр, ведущие нули, знаки, десятичные разделители, буквы между цифрами, Unicode digits, supplementary code points и null/exception. Сравнивать точные UTF-16 output и exception behavior; не добавлять поддержку десятичных чисел, которой нет в наблюдаемом фильтре.

## 2. Формирование строки диапазона зарплаты

**Класс:** тот же SalaryContentConverter, b(SalaryContentConverter,n0i,int):String, overload_index=0.

**FACT:** два nullable Long из getM/getN. Равные границы → одно число; две разные → число + пробел + en dash U+2013 + пробел + число; только нижняя/верхняя → ресурс salary_from/salary_to; обе отсутствуют → один из двух not-specified ресурсов по bit 4. Числа форматирует g7b.c(long). Evidence: `ev_e166e3689d6f08f2925fe78cea8b23e379ae4f39dfe602d5a9a56870b3cc55f8`.

**Связанные типы:** n0i (семантическое имя модели UNKNOWN), bfd (ресурсный provider), g7b.c (числовой formatter), R.string. Содержимое строковых ресурсов и helper не извлекались.

**Сложность:** низкая/средняя. Ветвление прозрачно, но точное форматирование и locale strings ещё не замкнуты. INFERENCE: getM/getN играют роли нижней/верхней границ по веткам, а не по восстановленным исходным именам полей.

**Проверка эквивалентности:** синтетическая модель и фиксированный resource adapter; комбинации null/equal/from-only/to-only/range, инвертированные границы, 0/negative/Long extrema; bit mask 0/4 и другие биты; отдельно g7b.c и реальные строки нужной locale. Проверять точные пробелы, dash и fallback. Подмена ресурсов fixtures проверяет только ветвление, а не полную locale-equivalence.

**Почему подходит:** небольшой UI-facing алгоритм без сети. До восстановления g7b.c и ресурсов демонстрация будет частичной. Runtime callers b отдельно не трассировались.

## 3. Потребление параметров поискового запроса

**Класс:** ru.hh.applicant.feature.search_vacancy.core.logic.presentation.converter.VacancySearchStateConverter.

**FACT:** b(String,LinkedHashMap) удаляет ключ через remove и возвращает первый элемент списка либо null; a вызывает b и заменяет null на пустую строку; c удаляет ключ и возвращает весь список либо EmptyList.INSTANCE. Это mutation, не обычный get. Evidence:

- a: `ev_b0ecbff48df0121213b7f0fcbb1e06b770f710d0e17d55f74cb405ca2c656306`;
- b: `ev_c68a7da06ec5047c1f1feebecf62ae35cea7ada814e153a97a2e7a667720c928`;
- c: `ev_3ac6e3ec551e1c7ab5465fba4129486dad1a3d1bae602236f573f9d97740ad92`.

**Связанные типы:** LinkedHashMap, List, Kotlin CollectionsKt.firstOrNull/EmptyList; converter.d(Uri, search.d) — подтверждённый caller b. Evidence: `ev_d93e551c1f1addb8090fc0de6f3458f3fc8b3bd93b10f50147cad294b07ce17c`. Тело d не декомпилировалось; весь URI parser не восстановлен.

**Назначение (INFERENCE):** пошаговый разбор параметров с удалением уже потреблённых полей. Точный формат всех фильтров и contract d UNKNOWN.

**Сложность:** низкая; минимальный запасной эксперимент, особенно для демонстрации сохранения side effects.

**Проверка эквивалентности:** сравнивать одновременно return/exception и map после вызова: отсутствующий ключ, null value, пустой список, один/несколько элементов, первый null, повторный вызов, соседние ключи и порядок оставшейся LinkedHashMap. Для c проверить идентичность возвращённого непустого списка, не только equals. Raw-type misuse проверять отдельно от типизированного domain contract. Никаких реальных поисковых запросов пользователя не требуется.

**Почему подходит:** контракт почти полностью виден, нет backend и Android context. Недостаток — алгоритм очень мал; это демонстрация точности реконструкции mutation semantics, не восстановления всего поиска.

## Отложенные темы

SalaryConverter, VacancySalaryFrequencyConverter, QuickWorkExperienceFilterConverter и VacancyViewCountConverter не выбраны как готовые алгоритмы: в просмотренном inventory у них только конструкторы. SearchFiltersViewModel слишком широк для первого эксперимента. UI-state mapper и сортировка вакансий в этом проходе не подтверждены небольшим изолированным телом.

## Условия следующего этапа

Предпочтителен кандидат 1: маленький контракт и подтверждённая связь с quick filters. Достаточное evidence следующего шага — точные helper prototypes/operations, согласованная матрица границ и независимое сравнение результатов. До такого сравнения не заявлять полную эквивалентность. Kotlin-реализация и её тесты пока не создавались.

