# Future test vectors (not executed)

These are reconstruction tests suggested by the observed contract. Expected results marked DERIVED come from static operation tracing, not execution of the APK.

## `a(String)`

| Case | Input | Expected | Confidence |
|---|---|---|---|
| Empty | `""` | `""` | FACT return-original branch |
| No digits | `"salary - ."` | unchanged | FACT return-original branch |
| One digit | `"7"` | `"7"` | DERIVED |
| Three digits | `"123"` | `"123"` | DERIVED |
| Four digits | `"1234"` | `"1 234"` | DERIVED |
| Six digits | `"123456"` | `"123 456"` | DERIVED |
| Seven digits and leading zero | `"0012345"` | `"0 012 345"` | DERIVED |
| Noise between digits | `"-12.34 руб."` | `"1 234"` | DERIVED; exact Character digits are ASCII here |
| Whitespace/noise but no digits | `" - . "` | unchanged including spaces | FACT return-original branch |
| BMP Arabic-Indic digits | `"١٢٣٤"` | retain iff deployed Character.isDigit(char) says true; likely grouping `"١ ٢٣٤"` | Conditional / runtime version |
| BMP superscript two | `"²"` | unchanged if `Character.isDigit('²')` false | Candidate negative classification; verify target JVM independently |
| Supplementary mathematical digit (surrogate pair) | e.g. U+1D7D9 | both surrogates removed; if no other digit, original input unchanged | DERIVED from code-unit API, confirm Android runtime |
| Null | null | NPE | FACT initial dereference |

## `b(...)`

Use fixed test resource adapter for branch tests, then separate Android-resource/runtime verification for labels. For number formatting assert against `NumberFormat.getInstance(Locale("ru","RU"))` on the target Java/Android runtime, not a host locale.

| m | n | mask | Expected path | Confidence |
|---|---|---:|---|---|
| null model | — | 0 | `view_vacancy_info_salary_not_specified` | FACT |
| null | null | 4 | `salary_not_specified` | FACT |
| null | null | 5, 8, -4 | `salary_not_specified` only when bit 4 set; otherwise view-vacancy string | FACT |
| 0 | 0 | any | formatted once | FACT |
| 1 | 2 | any | formatted m + space + en dash + space + formatted n | FACT |
| 2 | 1 | any | preserve order 2 then 1; no swap | FACT |
| Long.MIN_VALUE | null | any | `salary_from` + space + Russian-locale formatted value | FACT branch; rendered digits runtime-dependent |
| null | Long.MAX_VALUE | any | `salary_to` + space + Russian-locale formatted value | FACT branch; rendered digits runtime-dependent |
| negative m, distinct negative n | distinct | any | normal two-bound branch | FACT branch |
| equal Long extrema | same | any | one formatted number | FACT branch |

## Integration / contract checks

- Exact UTF-16 output, especially U+0020 grouping separator and U+2013 range dash.
- `a` leaves original unchanged only for zero accepted digits; with any accepted digit, discard all other characters.
- `b` uses bit 2 (numeric value 4), irrespective of unrelated mask bits.
- Test adapter failures/unknown localized values separately; those behaviors are outside the recovered method's guaranteed output.
