# Test vectors for independent Kotlin implementation

Vectors are derived from visible bodies, not executed tests. For d, use an Android Uri environment only if available; this stage creates no project.

## Primitive helpers

| Initial map | Call | Expected result | Map after |
|---|---|---|---|
| {} | a("k") | "" | unchanged |
| {k=[]} | a("k") | "" | {} |
| {k=["one","two"]} | a("k") | "one" | {} |
| {} | b("k") | null | unchanged |
| {k=[]} | b("k") | null | {} |
| {k=["one","two"]} | b("k") | "one" | {} |
| {k=["one","two"]} | c("k") | same list ["one","two"] | {} |
| {k=[]} | c("k") | same empty list object | {} |
| {k=["one"]} | b("k"); b("k") | "one", then null | {} after first |
| insertion order a,b,c | consume b, enumerate | a,c | remaining order a,c |

Directly supported by E3-E5.

## URI-to-state vectors

| Query | Expected observable | Confidence |
|---|---|---|
| no query | consumed lists empty; scalar defaults applied; residual query empty; old state supplies copied fields per constructor/copy path | Most rules E6; unspecified defaults UNKNOWN |
| text=dev | text enters normalizer; source-string path e rewrites literal plus to %20 | E6,E7; normalized value dependency-specific |
| area=1&area=2 | area list follows Uri values order | E6; Uri ordering not runtime-tested |
| period=bad&search_period=14 | period present wins; parse fails -> ALL_TIME, no retry of search_period | E6 |
| period=7&period=30 | scalar takes first value, parses as 7 | E4,E6 |
| experience=doesNotMatter&experience=between3And6 | sentinel removed, other item retained | E6 |
| label=with_salary&label=night_shifts&label=custom | two flags set; custom remains normal labels after helper | E6; exact normalization helper UNKNOWN |
| accept_temporary=TRUE | true | Boolean.parseBoolean path E6 |
| accept_temporary=yes | false | Boolean.parseBoolean path E6 |
| 3 of 4 box coords parse | primary geographic box null | E6; numeric helper UNKNOWN |
| both geo boxes complete | top_right/bottom_left form selected | E6 |
| unknown residual key | encoded unless in n1i.a; logged unless in n1i.b | E6; set membership UNKNOWN |
| salary present, currency absent, host absent | country source + currency repository fallback used | E6,E9; data-dependent |
| salary absent, currency absent | salary-driven currency fallback branch not taken | E6 |

## Exception probes

Null Uri and old state fail at generated null checks. Invalid numeric coordinates and malformed percent encoding need the exact helper/platform; outcome UNKNOWN. Keep resume/account/auth/PII cases out of the test suite.
