# Dependencies and open questions

## Minimal dependency graph

| Component | Role in salary behavior | Evidence |
|---|---|---|
| `ru.hh.shared.core.vacancy.converter.SalaryContentConverter` | `a(String)` and `b(converter,n0i,int)` | `ev_aab0e2cb006f8806d97ecf66a9ec195f8bc7b27ae5290da4dd6163729340b596`, `ev_62d4c356e4bcc8b90d5441b66984bf1ea5d537145abefb8d5441b66984bf1ea5d537145abefb8d5441b66984b` |
| `defpackage.m9g.J` | StringBuilder reverse | `ev_6c6ab9a6bd610b8aa720a0e0245fc71d367b016cc6b9ec70af39faa7b2adc7f1` |
| `defpackage.m9g.B` | Fixed-size chunking | `ev_6e9124ff55a4ddd50d5e383aec263b359620ed262c52dff0baa735ca0d4fb709` |
| `defpackage.bqf` with mode 19 | Converts chunk CharSequence to String | `ev_27b4c3b801627d3c1f1b99baac237df83a2df613217416658edaa07dc12dff6f` |
| `CollectionsKt.S` | Joins chunk list with supplied separator (call observed; library behavior is not separately decompiled here) | `ev_aab0e2cb006f8806d97ecf66a9ec195f8bc7b27ae5290da4dd6163729340b596` |
| `defpackage.n0i` | Nullable Long fields and getters (`m`, `n`; JADX reports getters `getM`, `getN`) | class `ev_075da5fe411aa05fbea513a7e7463f0ac0782ead9dcb12d2415a16c7fa908434`; getters `ev_c565f48a1628d2d3a9fe35a7398ad5514dcba4721f2b0c64359c1dcb08caee17`, `ev_8387747fb529487efd0ec941420049c55e866977f308849063565c9c44cb3da0` |
| `defpackage.g7b.c(long)` / `defpackage.n49.a` | NumberFormat and fixed `ru_RU` locale | `ev_0dc920a3b512b4c5c2472dca357a152c1d40456ae8b02b0baf847c61435afb90`, `ev_9d287e45a86fc5b8f96996e5d80117489c9e161020123d33ee935f8dc1aa1d53` |
| `defpackage.bfd` | Resource provider referenced for getString; provider field is owned by SalaryContentConverter | class metadata `ev_ee3ad0b2a35e3d0ffec26c68b731cc10a071eea618235a4db4ed205e83155cae`; method call in `b` evidence |
| `R.string` | IDs: `salary_from`, `salary_to`, `salary_not_specified`, `view_vacancy_info_salary_not_specified` | `ev_62d4c356e4bcc8b90d5441b66984bf1ea5d537145abefb8d5441b66984bf1ea5d537145abefb8d5441b66984b` |

## Application links

- `a(String)` has two direct invocations inside `QuickFiltersReducer.a`; REA refs evidence `ev_4608418afe21ccf413719e2ff5d046ce5718e2d4a6dfd882fcf2062da60350f2`. The reducer's salary path applies this to a displayed salary value before calling localized quick-filter label resources (`ev_e349e2822d6e12a27b007438453d8271cb4904e9a7a44147c94e24e36fef39f0`).
- `b(...)` has direct using nodes in `VacancyCardHeaderUiConverter`, `HiddenVacancyListViewModel$uiStateConverter$1`, and vacancy-info class `c`; refs evidence `ev_e823d39caeb7246cc9aab71cd01766478662fe25763072e361918cfbb8d30c44`.
- These are static links only. No app/UI runtime was started.

## Unresolved

1. Actual locale text for the four `R.string` entries: resource decode was attempted, but REA reported apktool launcher unavailable (`capability_unavailable`). No values should be invented.
2. Runtime-specific `Character.isDigit(char)` table for unusual BMP code points and exact Android API/Unicode data. Supplementary code point behavior is structurally determined by the char loop.
3. Platform NumberFormat rendering details for Long extremes are not replayed. Formatter locale and API are known.
4. No controlled runtime comparison was performed; static analysis is the requested evidence boundary.
