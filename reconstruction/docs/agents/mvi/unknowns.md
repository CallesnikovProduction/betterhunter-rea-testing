# Неизвестные и границы доказательств

| Вопрос | Статус | Что известно / что нужно для ответа |
|---|---|---|
| Как UI callback для `with_salary` формирует intent? | UNKNOWN | Найден reducer branch для `a.o0`, но Compose callback и его producer не связаны с этим типом. Нужна точечная трассировка callback в `SearchFiltersScreen`/ViewModel и вызова dispatch. |
| Какой `d` приводит к `a.C1116a(a.o0)`? | UNKNOWN | Actor выдаёт несколько типов `a` в зависимости от `d`; конкретная ветка salary не идентифицирована. |
| Как именно меняется salary-флаг в state? | UNKNOWN | Reducer вызывает `SearchFiltersStateInteractor.b(state.a)` и логирует `khe.p`; реализация нужной ветви interactor-а и модель `khe` не раскрыта. |
| Как state конвертируется в UI для этого поля? | UNKNOWN | Конструктор `SearchFiltersUiConverter` содержит `SalaryUiConverter`, но call site/метод преобразования в UI не восстановлен. |
| Что публикует `SearchFiltersResultPublisher` и кто получает? | UNKNOWN | ViewModel вызывает `a(...)`; класс-инвентарь вернул только constructor. |
| Гарантирует ли store последовательную обработку, immutable state и cancellation? | UNKNOWN | Конструктор feature и сигнатуры подтверждены, но реализация базового store и его lifecycle не исследованы. |
| Выполняется ли показанный код в production при конкретном клике? | UNKNOWN | Только статический разбор APK; runtime не наблюдался. |
| Используется ли конкретная MVI-библиотека? | UNKNOWN | Имена MVI-компонентов и их сигнатуры не доказывают зависимость/использование библиотеки. |

## Отрицательные границы

- Полный Android-клиент не декомпилировался. Исследованы только небольшие классы и методы среза фильтров.
- Compose экран не анализировался массово; конкретный callback не раскрыт.
- Backend, авторизация и закрытая серверная логика не исследовались.
- Coroutines/Flow подтверждены как ViewModel boundary/adaptation; RxJava подтверждён внутри bootstrapper/actor. Полный межпоточный порядок неизвестен.
