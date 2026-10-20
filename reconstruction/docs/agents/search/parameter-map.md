# Parameter map

Algorithm evidence: E6 in findings.md. Output names ниже описывают позиции внутренней модели, не API wire schema.

| URI query key | Read rule | State behavior |
|---|---|---|
| text | a: first or "" | text normalization, then regex processing |
| search_field | whole list | list |
| area, district | whole list | lists |
| salary | a | scalar |
| currency, fallback currency_code | first currency; if null first currency_code; if both null "" | currency code |
| employer_id | whole list | if empty, fallback to one head_employer_id via list wrapper |
| head_employer_id | first or null | fallback for employer_id |
| current_employer_id | first or null | null becomes "" |
| department | whole list | list |
| label | whole list, then CollectionsKt.i0 and filter/normalizer | remaining labels plus special flags |
| metro | whole list | list |
| salary_frequency | whole list | list |
| salary_mode | first or null | scalar |
| experience | whole list, removes doesNotMatter | remaining items keep order |
| order_by | first; if absent SearchOrderType.Companion.a().getId() | sort id |
| period, fallback search_period | first period; only if absent use search_period; int parse or ALL_TIME days | numeric period |
| professional_role, industry | whole lists | lists |
| adv_place_id | whole list | list |
| vacancy_id | first or "" | scalar |
| address | first or "" | scalar |
| sort_point_lat, sort_point_lng | first values, numeric helper | coordinate pair; helper details UNKNOWN |
| geohash | first or null | scalar |
| top_lat, bottom_lat, left_lng, right_lng | first values, numeric helper | box only if four parsed values |
| top_right_lat, top_right_lng, bottom_left_lat, bottom_left_lng | first values, numeric helper | alternate box; preferred when complete |
| accept_temporary | first; Boolean.parseBoolean; default false | boolean |
| hide_uninvited_response_vacancies | first bool plus a sensitive-state condition, not analyzed further | boolean; sensitive selector excluded from this research |
| employment_form, education, work_schedule_by_days, working_hours, work_format, driver_license_types, inclusiveness_types | whole lists | lists |
| polygon | first or null | scalar |
| other remaining keys | preserve map order, subject to n1i filters | residual encoded query; some logged as unknown |

## Labels

with_salary sets a boolean and is excluded from normal labels. accept_labor_contract, internship, night_shifts likewise become booleans. Other values pass through l2c.r; exact normalization remains unknown.

## Currency default

When salary is present and currency absent, d reads host from residual parameters. Recognized AppHost values map to CountryCode and CurrencyCode; unknown non-empty host falls back to RU. Missing/empty host uses CountryHostSource.k(); visible branch distinguishes BY and AZ, otherwise looks up RUR. CurrencyRepository.d(code) searches local dictionary and otherwise returns a default currency object. [E6,E9]

This is a client UI/domain default; it does not establish the server request contract.

## Recoverable vs server-only

URI restores locally encoded state: filters, sort/period, geo, label flags, currency fallback, and residual query. It does not recover vacancy corpus, index availability, ranking/relevance, server normalization, access rules, feature flags or HTTP results. Residual query does not prove it is sent to a server.

