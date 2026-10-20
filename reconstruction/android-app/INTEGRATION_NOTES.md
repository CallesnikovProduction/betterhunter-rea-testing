# BetterHunter integration

Open reconstruction/settings.gradle.kts as the single build root. Salary, vacancy, search and mvi are Kotlin/JVM modules; vacancy-ui is an Android library; android-app is the Compose application. The two existing vacancy composables moved to vacancy-ui, preserving package names. No source code is compiled in multiple modules. The alternative search/src/composeMain prototype is retained but intentionally not included; the Android shell owns search/filter screens.

## Build

Run ./build-local.ps1 from PowerShell. It runs all unit tests and :android-app:assembleDebug, copies the resulting APK to reconstruction/app-debug.apk, and removes its temporary drive alias in finally. The ASCII alias avoids a reproduced Windows/Gradle test-worker ClassNotFoundException with the Cyrillic project path; project files are not moved. Do not run a second build against the same outputs concurrently.

Toolchain verified on 2026-10-20: JDK 21.0.3, Gradle wrapper 9.6.0, AGP 9.4.1, Kotlin/Compose compiler 2.4.20, SDK 37, Compose UI/Foundation 1.12.1, Material3 1.3.1, Coroutines 1.11.0, JUnit Jupiter 5.13.4 for JVM tests and JUnit 4.13.2 for app tests. Set local sdk.dir to the installed Android SDK and JAVA_HOME to JDK 21. Dependencies are pinned in gradle/libs.versions.toml. Gradle's embedded Kotlin is separate from the application compiler.

Verified: help, all four JVM compilations, Android Compose compilation, 55 unit tests (salary 9, vacancy 6, search 23, mvi 11, app 6), and assembleDebug. No failures or skipped tests. The app tests exercise fixture adapters, salary presentation, search/filter/sort integration, three-page collection without duplicates, return-state preservation and error/empty/retry. MVI tests cover stale-search cancellation. These are local model/store tests, not rendered navigation or device tests.

Package: dev.betterhunter.app, version 0.1.0 (1), minSdk 26, target/compile SDK 37. Debug APK signature verified by apksigner. No installation, emulator launch or device smoke test was performed.

## Product boundary

UI pass on 2026-10-20: fixed dark graphite/green palette with a root Surface and matching system bars; rebuilt search controls, vacancy cards/details and grouped filter chips with a pinned apply action. Employment/schedule labels are localized. The original screen was captured from the connected phone in ui-review/before/screen.png. The updated UI compiled, all 55 unit tests remained green, and the delivery APK signature was verified. At the user's request the new APK was not installed; post-change visual/device checks remain NOT RUN.

The mock repository contains 18 synthetic vacancies. The app uses the existing local search engine, salary formatter and MVI pipeline through explicit module dependencies and adapters. Publication dates are fixed synthetic fixture metadata; salary comparisons do not perform currency conversion. No backend, authorization or real accounts are included. The APK has no INTERNET permission.

Search state is retained while visiting filters/details in the current composition. Process-death and Activity recreation persistence are not implemented. UI rendering and gestures remain unverified without an emulator/device. APK research evidence in other modules describes static observations, not equivalence to the original product.

Known build warnings: the local Unicode-path override is experimental; the SDK could not strip libandroidx.graphics.path.so, so it is packaged unchanged. Neither warning prevented compilation, tests, APK packaging or signature verification.
