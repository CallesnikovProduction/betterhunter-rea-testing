# Technology stack

Дата: 2026-10-19. Артефакт: `raw-source/base.apk`, `ru.hh.android` 26.38 (11487), minSdk 32, targetSdk 36.
SHA-256: `0ef900e1066cf63dd86e9924479bfe9bcc0a2669215984c4ccf97141c7953544`.
REA MCP 6.7.0 / JADX Headless 0.7.1. Только статические наблюдения.

**FACT** — наблюдается в метаданных, теле метода или статических ссылках. **INFERENCE** — интерпретация наблюдений. **UNKNOWN** — не установлено. Статическая связь не доказывает выполнение в production.

## Подтверждённые прикладные зависимости

| Технология | Статус и доказательство | REA evidence |
|---|---|---|
| Kotlin | FACT: Function1 в SearchFiltersViewModel; DefaultConstructorMarker и синтетический конструктор у FeedbackWizardRootModoMagritteScreen; Kotlin Collections в поисковом converter. INFERENCE: существенная часть клиентского слоя написана на Kotlin. | `ev_b0e46bf0b46574f08464c7220c521677a29a9e7285a4c3db3bce06710f1484ae`; `ev_4024f7a3d499fa882034355ea364f4598f3dd164e853629a49fede443c5d23ef` |
| Java | FACT: JVM/DEX и Java API используются. UNKNOWN: доля исходных Java-файлов; Java-представление JADX не доказывает исходный язык. | `ev_7a7c8c96882c1a9e44b92642d254e453114e5afba8f5e07dec3931c8d94004a1` |
| Jetpack Compose | FACT: AutosearchScreenKt.a/b принимают LazyListState и androidx.compose.ui.b. Это прямая типовая зависимость прикладного экрана, а не только наличие runtime. UNKNOWN: доля UI на Compose и фактическое достижение экрана. | `ev_22f49df76d16cb82963468d8971c02d1ba9d492f99a4d84412be6f8a90189053` |
| XML / Views | FACT: ReviewFragment.<init> передаёт R.layout.fragment_review в super и создаёт binding; onViewCreated(View, Bundle) есть в inventory. INFERENCE: UI смешанный Compose + Views/XML. Сам XML не извлекался. | `ev_3d7a94c24308da0c59c42be7eaf53c4d17678b658fc12f5c9c3527087b5320aa`; `ev_adc646ef2f9e20683c1330b1325b2435c53f03c0ca88daf3463baca07f5206a9` |
| Coroutines / Flow | FACT: RootRouter хранит Flow onEach transform и в конструкторе связывает его с coroutine utilities. UNKNOWN: dispatcher и cancellation-контракты всей системы. | `ev_1bb9a9435a54621f91832beee1768adeb3c31731e1674c17e2772d8df871831f`; `ev_4d5909d842f1f09f42984d2c5aa4e7603cb3b99196da510bb06539b58653f788` |
| RxJava | FACT: тот же RootRouter вызывает PublishSubject.create() и хранит PublishSubject<Pair<Integer,Object>>. Это прикладное использование, не только включённая библиотека. | `ev_4d5909d842f1f09f42984d2c5aa4e7603cb3b99196da510bb06539b58653f788` |
| DI: Toothpick / javax.inject | FACT: VacancyTextQueryConverter__Factory принимает toothpick.Scope; createInstance2 (DEX имя createInstance) создаёт converter; DictionaryDatabaseProvider.get реализует javax.inject.Provider. Это не основание объявлять __Factory классами Dagger/Hilt. | `ev_74358289eba8a2b92fbbdb8f2ca756ba30f9a9eb602b035d2d00e1b81dea8d29`; `ev_c5b1919602e5f070bd21ce26e8a520ec8cde1d0243518a6de2896403905f4af4`; `ev_d14e92c990274e7c7edbc8176d6ae01b463bb18c7ba5f569d438c97336c2e084` |
| OkHttp | FACT: OkHttpClientProvider.get строит okhttp3.n через n.a; NetworkApiBuilder хранит okhttp3.n и OkHttpClientFactory. UNKNOWN: все transport settings и runtime routes. | `ev_85cfed06c72ad5b679fb29426bfb3bca1a036839b535d9b80762059c60dba5e5`; `ev_baeb617b63cc3b32b20bd03a086cc8cf2f2c69fd003b713e0ed9d7ef51664e5e` |
| Room | FACT: DictionaryDatabaseProvider.get использует RoomDatabase builder для DictionaryDatabase, имя dictionary.db и asset database/dictionaries.db. Это описание кода; локальная БД и её строки не читались. | `ev_d14e92c990274e7c7edbc8176d6ae01b463bb18c7ba5f569d438c97336c2e084` |

## Присутствие библиотек и границы вывода

FACT: полный inventory содержит 54 588 строк классов; namespace counts ниже — строки inventory, не объём использования и не число уникальных исходных классов. Evidence: `ev_b13cc7c1e41fd99efc66992791eb2b33c4f503cc55363271a46eb0429afc7c02`.

| Namespace | Строк | Калибровка |
|---|---:|---|
| androidx.compose | 963 | Дополнительно подтверждена прикладная типовая зависимость выше |
| io.reactivex | 397 | Дополнительно подтверждён вызов PublishSubject.create |
| androidx.room | 86 | Дополнительно подтверждён builder базы словарей |
| androidx.datastore | 82 | FACT: присутствует; прикладное использование UNKNOWN |
| toothpick | 50 | Дополнительно подтверждены generated factory и Scope |
| androidx.lifecycle | 50 | FACT: присутствует; ViewModel в названиях недостаточно для установления всей lifecycle-иерархии |
| androidx.work | 38 | FACT: присутствует; планирование прикладных работ UNKNOWN |
| okhttp3 | 38 | Прямое построение клиента подтверждено |
| retrofit2 | 4 | FACT: присутствует, включая HttpException и adapter.rxjava2. INFERENCE: Retrofit вероятен; прикладная сборка Retrofit не подтверждена |

Дополнительная проверка retrofit2.a дала 4 using nodes (KotlinExtensions / defpackage.xo7), без NetworkApiBuilder: `ev_fc62387e76c77705e8e16ec83771146400ef9fe0368dc3b84a8b91c106f707bf`. Это не доказывает отсутствие Retrofit: имена обфусцированы, поиск ограничен одним классом.

FACT: есть namespaces com.arkivanov.mvikotlin и com.github.terrakok.modo. INFERENCE: Modo вероятен по screen/package names; конкретная привязка прикладного роутера к API Modo не прослежена. UNKNOWN: точная MVI-библиотека SearchFiltersFeature; само сочетание actor/reducer/bootstrapper не позволяет выбрать библиотеку. Альтернативные HTTP-клиенты, SharedPreferences и Dagger/Hilt не исключены и не подтверждены этим срезом.

