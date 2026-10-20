# Handoff — independent Kotlin search-state converter

## Implementable contract

1. Use an ordered multimap: LinkedHashMap<String, List<String>>.
2. Implement the three destructive extractors from behavior-spec.md: first-or-empty, first-or-null, list-or-empty.
3. Parse explicitly recognized search-state fields from parameter-map.md; preserve list order and scalar first-value behavior.
4. Separate URI parsing from model construction. The source-string path replaces literal + with %20 before parsing; keep this quirk in that adapter.
5. Preserve residual entries in insertion order. Apply n1i.a/b filters only after their membership is established; do not invent the sets.
6. Period precedence is period then search_period only when period is absent; an invalid selected value maps to ALL_TIME.
7. Inject currency resolution policy/data. Explicit currency wins; otherwise host/country and local repository supply a state default.
8. Represent coordinate parse outputs as nullable; create a bounds object only when all required values are present. Exact number parser behavior remains open.
9. Isolate text normalization behind an interface pending independent inspection of VacancyTextQueryConverter helpers.
10. Keep HTTP serialization and server behavior outside this module's compatibility claim.

## Evidence coverage

Helper semantics E3-E5. URI extraction/model handling E6. Migration string call path E7. Dependencies/currency fallback E2,E6,E9. Artifact/tool identity E1 and findings.md.

## UNKNOWN

- Members of n1i.a() and n1i.b().
- Text helpers, coordinate parser c.f, label normalizer l2c.r, generated model copy defaults.
- Complete model property names and untouched-field precedence.
- Exact host/country mapping and dictionary entries.
- Which static xrefs execute at runtime; dynamic callers.
- Android Uri malformed encoding, repeated mixed-case keys, empty names, fragment edge cases.
- Whether residual values reach any HTTP request.
- Backend vacancy search, filtering, ranking and server defaults.

## Scope guard

Excludes accounts, resumes, authorization, PII, closed API traffic, service bypass and Kotlin project creation. Resume-related model behavior was not researched. No Kotlin sources or build artifacts were created.
