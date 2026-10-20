# Роли компонентов

Evidence ID относится к статическим наблюдениям REA/JADX над `raw-source/base.apk`. `FACT` — наблюдаемое тело/сигнатура; `INFERENCE` — осторожная интерпретация; `UNKNOWN` — связь или семантика не установлена.

| Компонент | Подтверждённая роль | Evidence |
|---|---|---|
| `SearchFiltersViewModel` | Поля содержат `SearchFiltersFeature`, `SearchFiltersUiConverter`, `SearchFiltersResultPublisher`, analytics и прочие presentation-зависимости. Конструктор устанавливает coroutine observer. Метод `Z5(SearchFiltersFeature.b)` обрабатывает `b.a` как применение search model: публикует результат и делегирует навигационное действие; прочие варианты ведут к локальным callback/interactor вызовам. | `ev_c0027009687642bbebb965cda87524ec952dcc9610016ab0f0e326ba1bf16a32`, `ev_d74b721be154fa8f901bb8a4655404aeb5fd78f259f49016ca6e079abb731bc4`, `ev_8dbec6044e5de6ae27256384180945b97d3b2940f370e8fc7b5ac9428d92aa29` |
| `SearchFiltersFeature` | Конструктор принимает Actor, Reducer, Bootstrapper. Сигнатуры компонентов показывают `c` как state, `d` как actor input, `a` как actor output/reducer input и `b` как ViewModel-observed event family. Это role mapping по месту использования; полный базовый store закрыт/обфусцирован. | `ev_f8c842214ac77facbc3a910f88c067427644e50c6894d4ff7503ab69db9c79a1`, `ev_6fdee55ceb5ff293e37e5e1973571aaab9d44d769c007e311665fee18265401c`, `ev_e3c04046176d621334e5e2980327c108b813d0337bf455a62475ddef178e5472`, `ev_8dbec6044e5de6ae27256384180945b97d3b2940f370e8fc7b5ac9428d92aa29` |
| `SearchFiltersActor` | `invoke(c,d)` возвращает Rx Observable `a`. Ветка `d.e` вызывает `SearchFiltersInitInteractor`; ветка `d.m` запускает vacancy-count update; отдельные `d` приводят к `a` с interactor результатом или синхронным `Observable.just`. Зависимости включают Rx subject для события, interactor-ы и schedulers. | `ev_6fdee55ceb5ff293e37e5e1973571aaab9d44d769c007e311665fee18265401c`, `ev_36dc75d8df4cdf323ef7f782d8d213cea9e512820cdbb1e8e68eb3c56daefbc0` |
| `SearchFiltersReducer` | `invoke(c,a)` имеет явные ветви для результатов `a` и строит новые `c` через `SearchFiltersFeature.c.a(...)` либо специализированную ветку. `a(state, a.C1116a)` использует `SearchFiltersStateInteractor.b(state)`; до этого обрабатывает аналитику по типам interactor-событий. | `ev_e3c04046176d621334e5e2980327c108b813d0337bf455a62475ddef178e5472`, `ev_aa14426a56fe532852a240d128fc5edcc625222bd011264948240427cb4fa9d4`, `ev_c788a71c03c99d1d3a92c3940ab98b602b2414a966d261122999f9bb2ee65c42` |
| `SearchFiltersBootstrapper` | Возвращает Rx `Observable<d>`. Мержит преобразованные источники и `startWith(d.e)`; задаёт scheduler-ы. Конкретные объединённые потоки — suggestions, режим/профиль, selections и другие источники, но не все были раскрыты до UI. | `ev_4a05c123592166f9b9bf24f7c1fad529d06eb8a7c77c1797276d3a7f05d71315` |
| `SearchFiltersUiConverter` | Конструктор собирает набор узких converter-ов, в том числе `SalaryUiConverter`, `WorkExperienceUiConverter`, `RegionUiConverter` и `ApplyButtonUiConverter`. Конкретное преобразование и вызов в ходе выбранного сценария неизвестны. | `ev_39efce506872c96bd95e99a1234f2df3ec50250d47866793f0384012029a43a0` |
| `SearchFiltersResultPublisher` | ViewModel вызывает `a(mfe.b(...))` при результате применения и условно ещё `a(mfe.a(...))` при изменении поиска. Инвентарь класса показал только конструктор, поэтому транспорт/подписчики неизвестны. | `ev_ba5f962cfbc4a83163840fb551167331a213cd2bd77a5016e0faceb7d92e5e34`, `ev_8dbec6044e5de6ae27256384180945b97d3b2940f370e8fc7b5ac9428d92aa29` |

## Контракт MVI: уровень уверенности

- `c` — state: **INFERENCE высокой уверенности** по параметру reducer и actor и его преобразованию.
- `d` — actor input / intent-like event: **INFERENCE высокой уверенности** по `Bootstrapper → Observable<d>` и `Actor.invoke(state,d)`.
- `a` — actor output / message: **INFERENCE высокой уверенности** по `Actor.invoke → Observable<a>` и `Reducer(state,a)`.
- `b` — feature/ViewModel outward event family: **INFERENCE средней уверенности** по `ViewModel.Z5(b)`; точный контракт и producer не прослежены.
- Полная цепочка диспетчеризации, сериализация и гарантия immutable state: **UNKNOWN**.
