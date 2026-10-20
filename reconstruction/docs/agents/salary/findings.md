# Salary domain findings

Scope: static inspection of `raw-source/base.apk` (`ru.hh.android` 26.38 / 11487), REA 6.7.0 with JADX Headless 0.7.1. APK SHA-256 `0ef900e1066cf63dd86e9924479bfe9bcc0a2669215984c4ccf97141c7953544`. No execution.

Evidence IDs below are REA evidence. FACT = directly observed in shipped-artifact evidence; INFERENCE = deduction from operations; UNKNOWN = unresolved by this pass.

## `SalaryContentConverter.a(String): String`

| Claim | Status | Evidence |
|---|---|---|
| Body complete in Java, no fallback. It dereferences the argument immediately (`str.getClass()`), so null fails with NPE rather than producing a string. | FACT | `ev_aab0e2cb006f8806d97ecf66a9ec195f8bc7b27ae5290da4dd6163729340b596` |
| Iterates `String.length()/charAt(i)`: input is processed by UTF-16 code unit. Keeps only units for which `Character.isDigit(char)` is true. If none survive, returns the original string unchanged. | FACT | same |
| If any digit survives, every non-digit unit (sign, punctuation, decimal/group separator, whitespace, letters) is removed; digits are never parsed, so leading zeroes remain. | FACT for filter; INFERENCE for consequences | same |
| `m9g.J` reverses a StringBuilder; `m9g.B(3, ...)` chunks left-to-right into sizes at most 3; `bqf(19)` maps a CharSequence to `toString()`. Chunks are joined by literal ASCII space and the joined result reversed. | FACT operations; INFERENCE right-to-left grouping | `ev_6c6ab9a6bd610b8aa720a0e0245fc71d367b016cc6b9ec70af39faa7b2adc7f1`, `ev_6e9124ff55a4ddd50d5e383aec263b359620ed262c52dff0baa735ca0d4fb709`, `ev_27b4c3b801627d3c1f1b99baac237df83a2df613217416658edaa07dc12dff6f`, `ev_aab0e2cb006f8806d97ecf66a9ec195f8bc7b27ae5290da4dd6163729340b596` |
| Because digit filtering is per `char`, BMP decimal digits can survive; supplementary-plane digits arrive as surrogate halves, neither of which is a digit. Exact classification of less-common code points depends on the Android runtime's Character Unicode data. | FACT for operation shape; UNKNOWN for runtime-version edge table | `ev_aab0e2cb006f8806d97ecf66a9ec195f8bc7b27ae5290da4dd6163729340b596` |
| `QuickFiltersReducer.a` contains two direct static invokes of `a(String)` at classes6.dex code-unit offsets 859 and 894; its salary branch uses the result in quick-filter titles. Static references do not prove a runtime path executed. | FACT | refs `ev_4608418afe21ccf413719e2ff5d046ce5718e2d4a6dfd882fcf2062da60350f2`; reducer `ev_e349e2822d6e12a27b007438453d8271cb4904e9a7a44147c94e24e36fef39f0` |

## `SalaryContentConverter.b(...): String`

| Claim | Status | Evidence |
|---|---|---|
| Null model means both bounds are null. Otherwise reads nullable Longs via `getM()` and `getN()` (backing fields `m`, `n`). Names suggest lower/upper, but field semantics are not proven by declarations alone. | FACT / INFERENCE | `ev_62d4c356e4bcc8b90d5441b66984bf1ea5d537145abefb8d5441b66984bf1ea5d537145abefb8d5441b66984bf1ea5d537145abefb8d5441b66984b` |
| Equal non-null values render once; two unequal non-null values render `format(m) + " " + U+2013 + " " + format(n)`. No bound ordering check or swap. | FACT | same |
| First-only uses `salary_from + " " + format(m)`; second-only uses `salary_to + " " + format(n)`; both absent use one not-specified string selected by bit 4. | FACT | same |
| Mask expression is `(mask & 4) != 0`; all other bits have no influence on the selection. | FACT | same |
| `g7b.c(long)` uses `NumberFormat.getInstance(n49.a).format(long)`. `n49.a` is constructed as `Locale("ru", "RU")`. | FACT | formatter `ev_0dc920a3b512b4c5c2472dca357a152c1d40456ae8b02b0baf847c61435afb90`; locale field `ev_a5f5adcc418a40f84ce295775be73a78d7594ee43f615c8b52a50bced1fd63e4`; initializer `ev_9d287e45a86fc5b8f96996e5d80117489c9e161020123d33ee935f8dc1aa1d53` |
| Three static callers found: VacancyCardHeaderUiConverter, HiddenVacancyListViewModel$uiStateConverter$1, and vacancy-info `c`; no assumption is made about runtime execution. | FACT | `ev_e823d39caeb7246cc9aab71cd01766478662fe25763072e361918cfbb8d30c44` |
| The four resource IDs used by the method are identified, but localized resource values were not decoded: REA's Android resource decoder could not run because apktool launcher is unavailable. | UNKNOWN | decode attempt returned `capability_unavailable`; no successful resource evidence |

## Direct call evidence

`a(String)` caller is QuickFiltersReducer. `b(...)` caller reference inventory found 3 containing classes listed above. The `b` body itself is sufficient to establish branches, but no caller bodies were expanded in this focused pass.
