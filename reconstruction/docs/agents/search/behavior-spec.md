# Behavior specification

Источники: E3-E6 в findings.md.

## 1. Primitive map converters

Фактический вход: LinkedHashMap<String, List<String>>. Generic типы helper-ов стёрты в DEX.

| Helper | Операция | Результат |
|---|---|---|
| b(key, map) | list=map.remove(key); если list ненулевой, firstOrNull(list), иначе null | Nullable first value |
| a(key, map) | вызывает b; null заменяет на "" | Non-null String |
| c(receiver,map,key) | list=map.remove(key); null заменяет на Kotlin EmptyList; иначе возвращает тот же list | List |

Edge cases:
- Отсутствующий ключ: a -> "", b -> null, c -> EmptyList.
- Пустой список: a -> "", b -> null, c -> тот же пустой список.
- Непустой список: a/b выбирают индекс 0, остальные игнорируют; c возвращает весь список, сохраняя порядок и ссылку.
- Повторный вызов того же ключа после первого удаления ведёт себя как отсутствующий.
- Map мутируется даже при пустом list; элементы извлечённого списка helper-ы не меняют.
- Null map/key явно не обрабатываются; ожидаемая категория NPE, точное сообщение/runtime неизвестны.
- Null list трактуется как отсутствующий key. В d Android Uri штатно создаёт list values, поэтому null-entry не ожидается.
- Нарушение runtime-типа может дать ClassCastException.

## 2. URI-to-state d(uri, oldState)

1. Null-check для uri и oldState.
2. Создаётся новая LinkedHashMap: для каждого Uri query name значение равно Uri.getQueryParameters(name).
3. Известные ключи удаляются helper-ами и преобразуются по parameter-map.md.
4. Остаток кодируется через Uri.Builder в порядке map entries, внутри ключа — в порядке списка.
5. n1i.a() ключи не попадают в остаточный query. Ключи n1i.b(), если не в первом наборе, входят в query, но не считаются неизвестными. Остальные ключи логируются как unknown.
6. При неизвестных ключах logger получает exception-like объект с русским сообщением. Это локальное логирование, не запрос.
7. Создаётся новая search model с извлечёнными значениями и частью значений из oldState.
8. Выполняется text normalization и regex-ветка; затем возвращается новая модель через copy helper.

### Nullable, malformed, repeated

- Отсутствующие scalar-поля дают null либо указанное default; salary helper a всегда даёт строку.
- Повторные значения остаются list; scalar helpers выбирают первый. Порядок Uri list отдельным runtime тестом не подтверждался.
- Пустое значение x= не равно отсутствующему ключу. Для a оба случая могут дать ""; b сохраняет пустую строку как непустое nullable значение.
- period использует toIntOrNull; отсутствующее или unparsable значение получает ALL_TIME days. Если period присутствует, но invalid, search_period не используется как fallback.
- Координаты идут через c.f(String); malformed, NaN/Infinity semantics UNKNOWN. Box создаётся только при четырёх распознанных координатах.
- Boolean.parseBoolean: только строка true без учёта регистра даёт true; null/прочее false.
- Сортировка/дедупликация значений не добавляются без evidence.

## 3. Side effects and purity

Наблюдаемое: локальная map удаляет keys; logger вызывается при unknown остатке; currency repository и country host источник читаются при salary без explicit currency; text converter — injected dependency. [E2,E6,E9]

Видимый код не меняет Uri или старый state и не выполняет HTTP. Скрытые эффекты внутри нераскрытых dependencies статически исключить нельзя.
