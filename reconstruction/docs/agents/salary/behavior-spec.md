# Behavior specification: salary display

## 1. `a(String)` digit grouping

Input is non-null String. Null dereference fails (NPE). Scan UTF-16 code units from index 0 to `length-1`; append a unit iff `Character.isDigit(char)` is true.

If the filtered sequence is empty, return the original input exactly. Otherwise reverse the filtered sequence, split from the left into chunks of at most 3, join chunks with one U+0020 space, then reverse the joined string. Equivalently, group retained digits from the right in triads, preserving their original order and leading zeroes.

| Input class | Expected behavior | Status / evidence |
|---|---|---|
| Empty or no digits | Original unchanged | FACT `ev_aab0e2cb006f8806d97ecf66a9ec195f8bc7b27ae5290da4dd6163729340b596` |
| 1-3 retained digits | Same digit sequence | INFERENCE from observed pipeline, same evidence plus helper IDs in findings |
| 4+ retained digits | Right-aligned triads separated by U+0020 | INFERENCE from observed pipeline |
| Signs, punctuation, separators, whitespace, letters mixed with digits | Remove all non-digit units, then group | FACT filter + INFERENCE outcome |
| Leading zeroes | Preserve and group as ordinary digits | FACT filter + INFERENCE outcome |
| BMP digit characters | Keep iff runtime `Character.isDigit(char)` says true | FACT |
| Supplementary digit code point | Surrogate units tested separately and discarded | FACT operation shape; runtime cross-version detail UNKNOWN |
| Null | NPE at initial dereference | FACT |

Examples implied by the operation chain (not executed against the APK): `"1234" -> "1 234"`; `"0012345" -> "0 012 345"`; `"-12.34 руб." -> "1 234"`. Treat these as derived vectors, not runtime replay.

## 2. `b(converter, model, mask)` range string

Let `m = model?.getM()` and `n = model?.getN()`. Let `F(x)` be `NumberFormat.getInstance(Locale("ru", "RU")).format(x)`.

| m | n | Output expression | Status / evidence |
|---|---|---|---|
| non-null | same value | `F(m)` | FACT `ev_62d4c356e4bcc8b90d5441b66984bf1ea5d537145abefb8d5441b66984bf1ea5d537145abefb8d5441b66984b` |
| non-null | different non-null | `F(m) + " " + "–" + " " + F(n)` (en dash U+2013) | FACT |
| non-null | null | localized `salary_from` + U+0020 + `F(m)` | FACT |
| null | non-null | localized `salary_to` + U+0020 + `F(n)` | FACT |
| null | null (including null model) | `salary_not_specified` if `(mask & 4) != 0`; otherwise `view_vacancy_info_salary_not_specified` | FACT |

`F` formats a Long using Russian/Russia locale. Exact grouping glyph and `Long.MIN_VALUE`/`MAX_VALUE` presentation are delegated to the platform NumberFormat implementation; static code proves formatter selection, not a frozen output byte sequence. Resource labels and their localization are UNKNOWN pending successful resource decode.

No branch validates that `m <= n`; inverted inputs preserve argument order. Zero and negative values take the normal formatting branches. The method uses exact Long equality for the equal-bounds branch.

## 3. Scope limits

No empty-string fallback is applied by `b` itself. Resource provider invocation behavior on missing resources, configuration changes, or provider exceptions is not independently specified here. The discovered static caller links do not establish production execution.
