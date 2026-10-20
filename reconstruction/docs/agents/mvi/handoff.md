# Передача результатов

## Что можно использовать

- Подтверждена карта ролей: ViewModel + presentation converter/publisher; Feature собран из Actor, Reducer и Bootstrapper; Actor и Bootstrapper работают с Rx Observable; ViewModel содержит coroutine Flow bridge.
- В reducer подтверждён конкретный фрагмент для `a.o0`: аналитика метки `with_salary` получает APPLY/RESET в зависимости от текущего `khe.p`; новое state вычисляется через `SearchFiltersStateInteractor.b(state.a)`.
- Bootstrapper начинает поток с `SearchFiltersFeature.d.e`; actor обрабатывает эту команду через `SearchFiltersInitInteractor`.

Подробности и Evidence IDs: [architecture.md](architecture.md), [components.md](components.md), [state-transitions.md](state-transitions.md).

## Статус критерия

Карта архитектурных ролей восстановлена. Полный end-to-end пользовательский сценарий не подтверждён: отсутствует связь UI callback → dispatch `d` → `a.o0`, а смысл точного state update скрыт за interactor-ом. Диаграммы помечают эти рёбра как `UNKNOWN`/`INFERENCE`, поэтому результат не утверждает, что нажатие было прослежено целиком.

## Повторяемый следующий точечный probe

1. Найти в `SearchFiltersViewModel`/`SearchFiltersScreen` callback, связанный с salary UI, не раскрывая остальной Compose экран.
2. Проверить producer `SearchFiltersFeature.d` и связать его с actor output `SearchFiltersFeature.a.C1116a(a.o0)`.
3. Прочитать только нужную ветку `SearchFiltersStateInteractor.b` и converter salary; обновить sequence diagram, если связи подтверждены.

Статический анализ выполнен через REA MCP / JADX Headless 0.7.1 для `raw-source/base.apk`; APK не выполнялся. Ни один файл вне `agents/mvi/` не создавался или не изменялся.
