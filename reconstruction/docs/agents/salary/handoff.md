# Handoff: salary display reconstruction

Implement from [behavior-spec.md](behavior-spec.md); do not infer the unavailable localized resource text. `a(String)` is a UTF-16-char `Character.isDigit` filter, then right-to-left groups of three with ASCII spaces; if no digit survives, return input unchanged. Null fails. Helper chain is confirmed, not guessed.

`b(...)` branches on nullable Long fields `m` and `n`: equal renders once; both distinct renders `m – n` with spaces around U+2013; m-only uses `salary_from`; n-only `salary_to`; neither uses one of two not-specified resources based only on `(mask & 4) != 0`. Long formatting uses `NumberFormat.getInstance(Locale("ru", "RU"))`.

Use `findings.md` for per-claim REA evidence IDs/status and [dependencies.md](dependencies.md) for boundaries. Resource values remain UNKNOWN because apktool was unavailable. Static references show QuickFiltersReducer for `a` and three callers for `b`; no runtime equivalence claim was made. No Kotlin code or tests were created in this research phase.
