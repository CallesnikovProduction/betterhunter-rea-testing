# Архитектура поисковых фильтров

## Область и вывод

Исследован только статический срез APK `base.apk` 26.38 (11487), SHA-256 `0ef900e1066cf63dd86e9924479bfe9bcc0a2669215984c4ccf97141c7953544`. Кодовые тела получены через REA / JADX Headless 0.7.1; приложение не запускалось.

**Подтверждено:** `SearchFiltersViewModel` связывает feature с presentation converter и publisher; feature создаётся из actor, reducer и bootstrapper. Reducer принимает пару `SearchFiltersFeature.c` + `SearchFiltersFeature.a`, возвращает `SearchFiltersFeature.c`; actor принимает state + `SearchFiltersFeature.d` и возвращает Rx `Observable` результатов `SearchFiltersFeature.a`. Bootstrapper возвращает Rx `Observable<SearchFiltersFeature.d>`, объединяя несколько источников и начиная с `d.e`. Evidence: `ev_d74b721be154fa8f901bb8a4655404aeb5fd78f259f49016ca6e079abb731bc4`, `ev_f8c842214ac77facbc3a910f88c067427644e50c6894d4ff7503ab69db9c79a1`, `ev_6fdee55ceb5ff293e37e5e1973571aaab9d44d769c007e311665fee18265401c`, `ev_e3c04046176d621334e5e2980327c108b813d0337bf455a62475ddef178e5472`, `ev_6ae4527b4b3fecde931042e5b5b8a0d7d1dfd8873a1f2b25b471d2ad20395e82`.

**INFERENCE:** это MVI-подобная схема с state, action/result, intent/command ролями и отдельным ViewModel presentation-слоем. Эти роли выводятся из сигнатур и тел, а не только из имён. Конкретная MVI-библиотека не установлена: наличие MVI-зависимостей или именованных классов само по себе этого не доказывает.

## Границы

- ViewModel отдаёт state в callback/converter и обрабатывает `SearchFiltersFeature.b` как UI-события/результаты: например, применение фильтров публикует результат через `SearchFiltersResultPublisher`, затем делегирует навигационное действие в зависимости от режима поиска. `ev_51aa4c3e7dea748a88842a183c41816dcc9c77c53121c6b60c7ecde6d034f80a`, `ev_8dbec6044e5de6ae27256384180945b97d3b2940f370e8fc7b5ac9428d92aa29`.
- Feature — композиционная граница трёх частей. Полная реализация базового store и его scheduling не восстановлена. `ev_f8c842214ac77facbc3a910f88c067427644e50c6894d4ff7503ab69db9c79a1`.
- Actor — преобразует входные `d` в асинхронные Rx результаты `a`; зависимости включают interactor-ы, словари, локальные настройки, аналитику и Rx `PublishSubject<Unit>`. `ev_6fdee55ceb5ff293e37e5e1973571aaab9d44d769c007e311665fee18265401c`.
- Reducer — вычисляет новое состояние из текущего состояния и `a`; часть изменений также направляет в analytics. `ev_e3c04046176d621334e5e2980327c108b813d0337bf455a62475ddef178e5472`, `ev_c788a71c03c99d1d3a92c3940ab98b602b2414a966d261122999f9bb2ee65c42`.
- Bootstrapper — объединяет наблюдения/источники результатов в Rx Observable, добавляет стартовый `d.e`, применяет `subscribeOn` и `observeOn`. `ev_4a05c123592166f9b9bf24f7c1fad529d06eb8a7c77c1797276d3a7f05d71315`.
- `SearchFiltersUiConverter` — конструктор принимает специализированные converter-ы для зарплаты, опыта, региона и других полей. Как именно и где они преобразуют state в UI-модель, из доступного тела не установлено. `ev_39efce506872c96bd95e99a1234f2df3ec50250d47866793f0384012029a43a0`.
- `SearchFiltersResultPublisher` — ViewModel держит ссылку и вызывает `a(...)` при обработке результата применения. Тело publisher не предоставлено decompiler-ом (инвентарь нашёл конструктор); семантика канала публикации неизвестна. `ev_ba5f962cfbc4a83163840fb551167331a213cd2bd77a5016e0faceb7d92e5e34`, `ev_8dbec6044e5de6ae27256384180945b97d3b2940f370e8fc7b5ac9428d92aa29`.

## Реактивные технологии

**FACT:** внутри исследованного feature-пути широко используются `io.reactivex.Observable` / `Single`, в том числе actor и bootstrapper. ViewModel запускает coroutine Flow сбор через `e34.v(mhi.a(this), ...)`, а также адаптирует Rx-источник в Flow посредством `RxFlowExtKt.a(...)`. Это подтверждает сосуществование RxJava и Coroutines/Flow на границе ViewModel. `ev_36dc75d8df4cdf323ef7f782d8d213cea9e512820cdbb1e8e68eb3c56daefbc0`, `ev_d74b721be154fa8f901bb8a4655404aeb5fd78f259f49016ca6e079abb731bc4`.

**UNKNOWN:** общая политика отмены, lifecycle disposal и порядок конкурентных событий в store. Наличие `subscribeOn`/`observeOn` у bootstrapper и отдельных actor operations не доказывает полный scheduling всего feature.

## Что полезно для BetterHunter

Переносимые идеи: выделить typed state, входные намерения, асинхронные результаты, чистый переход состояния и отдельную презентационную модель; держать внешние side effects в actor/effect boundary; описать bootstrap как явный начальный источник событий; тестировать reducer отдельно от UI и инфраструктуры. Это рекомендации (INFERENCE/design), не утверждение о превосходстве или полном устройстве исследуемого клиента.

Не переносить обфусцированные типы, конкретную библиотеку или смешение Rx/Flow без необходимости. Для BetterHunter можно выбрать один основной поток событий, если интеграции не требуют Rx; сохранять отдельный адаптер только там, где это подтверждено контрактом.

## Ограничения

Статика APK показывает возможные связи кода, но не подтверждает production-выполнение. Не исследовались аккаунты, авторизация, серверная логика, остальные экраны и полный Compose UI. Полный UI → intent → actor → reducer путь для одного клика не установлен; см. [unknowns.md](unknowns.md).
