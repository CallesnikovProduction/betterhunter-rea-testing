![BetterHunter](assets/bh-logo.png)

# BetterHunter

Хотел найти работу. Нашёл APK, декомпилятор и повод написать ещё одно Android-приложение.

**BetterHunter** — эксперимент с reverse engineering Android-приложения `ru.hh.android` и сборкой собственного MVP на Kotlin. Сначала посмотрели, как устроены отдельные клиентские механизмы, потом собрали локальный поиск вакансий. Где-то между этими этапами появилась тёмная тема. Приоритеты расставлены.

Баннер уже обещает карьеру. В приложении пока **18 вымышленных вакансий** и локальный mock gateway. На собеседование никто не позовёт, зато фильтры работают.

## Что получилось

- Поиск по тексту и фильтры по региону, опыту, формату работы и зарплате.
- Сортировка, пагинация и загрузка следующих страниц без дублирования карточек.
- Карточки вакансий, экран деталей и возврат к результатам поиска.
- Форматирование зарплат, включая диапазоны и отсутствующие значения.
- Тёмный Compose UI с зелёным акцентом, чипами и кнопками, которые поместились в экран хотя бы по замыслу.
- Демо-сценарии пустой выдачи и ошибки с повторной загрузкой. Страдать можно без интернета.

На этапе сборки MVP прошли **55 unit-тестов**. Новый UI скомпилирован; визуальную проверку последней версии на устройстве ещё не проводили. Честность тоже входит в стек.

## Technology Stack ⚡

### 📱 Android & UI

<p>
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android">
  <img src="https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose">
  <img src="https://img.shields.io/badge/Material_3-757575?style=for-the-badge&logo=materialdesign&logoColor=white" alt="Material 3">
</p>

### 🧩 Architecture & State

<p>
  <img src="https://img.shields.io/badge/Coroutines-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin Coroutines">
  <img src="https://img.shields.io/badge/Flow-434C5E?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin Flow">
  <img src="https://img.shields.io/badge/MVI-2563EB?style=flat-square" alt="MVI">
  <img src="https://img.shields.io/badge/Android_Navigation-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android Navigation">
  <img src="https://img.shields.io/badge/Multi--module-434C5E?style=flat-square" alt="Multi-module Gradle build">
</p>

JVM-модули держат модели, зарплатные форматтеры и поисковую логику. MVI связывает действия и состояние, Android-модули отвечают за экраны. Mock gateway позволяет проверить весь локальный путь без настоящего сервера и чужих аккаунтов.

### 🛠️ Build & Tests

<p>
  <img src="https://img.shields.io/badge/Gradle-02303A?style=flat-square&logo=gradle&logoColor=white" alt="Gradle">
  <img src="https://img.shields.io/badge/AGP-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android Gradle Plugin">
  <img src="https://img.shields.io/badge/JDK_21-ED8B00?style=flat-square&logo=openjdk&logoColor=white" alt="JDK 21">
  <img src="https://img.shields.io/badge/JUnit-25A162?style=flat-square&logo=junit5&logoColor=white" alt="JUnit">
  <img src="https://img.shields.io/badge/Coroutines_Test-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Coroutines Test">
</p>

## Чем разбирали APK 🔬

<p>
  <img src="https://img.shields.io/badge/REA_MCP-234F3D?style=for-the-badge" alt="Reverse Engineer Anything MCP">
  <img src="https://img.shields.io/badge/JADX_Headless-0.7.1-2C2C2C?style=for-the-badge&logo=android&logoColor=white" alt="JADX Headless 0.7.1">
  <img src="https://img.shields.io/badge/JDK_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="JDK 21">
  <img src="https://img.shields.io/badge/Codex-181717?style=for-the-badge" alt="Codex">
</p>

Через REA MCP и JADX Headless выборочно изучали пакеты, сигнатуры, методы и статические ссылки. Для выводов сохраняли evidence ID. Весь продукт в исходники не выгружали: интересовали небольшие механизмы, которые можно понять и проверить отдельно.

Из APK удалось разобрать отдельные преобразования строк и поисковых параметров. Модели BetterHunter, mock-ранжирование, правила локального поиска и оформление интерфейса — собственный дизайн эксперимента. Серверную выдачу HeadHunter по клиентскому APK мы, увы, не изобрели заново.

## Что лежит внутри

```text
reconstruction/
├── salary/       # Форматирование зарплат
├── vacancy/      # Модели вакансий, fixtures и UI mapping
├── search/       # Парсер, фильтры и локальный gateway
├── mvi/          # Store, actor, reducer и состояние
├── vacancy-ui/   # Compose-карточки и детали
└── android-app/  # Приложение и интеграция модулей
```

Бэкенд, авторизация и реальные аккаунты в этот эксперимент не вошли. Зато вакансии не протухают: мы их сами придумали.

## Как запустить

Спросите у агента. Он это собирал, ему и объяснять.

Для тех, кто предпочитает читать файлы: [заметки по сборке](reconstruction/android-app/INTEGRATION_NOTES.md).

---

[Apache 2.0](LICENSE) · Учебный эксперимент, с HeadHunter не связан.
