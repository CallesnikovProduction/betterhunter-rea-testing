# Dependencies and bounded call graph

## Graph

SearchStateMigrationMediator.a(String)
  -> VacancySearchStateConverter.e(String)
      -> Uri.parse(replace("+", "%20"))
  -> d.Companion.a() [default state]
  -> VacancySearchStateConverter.d(Uri, d)
      -> Uri query names/values -> local LinkedHashMap
      -> a/b/c -> remove/consume known keys
      -> CurrencyRepository + CountryHostSource [conditional default currency]
      -> unknown-param logger [conditional]
      -> VacancyTextQueryConverter + regex [text normalization]
      -> d constructor / d.b copy [new model]

Evidence: E2-E7, E9. This graph is static, not runtime proof.

| Dependency | Observed role | Evidence / limit |
|---|---|---|
| VacancyTextQueryConverter | Injected field; d invokes query normalization after model construction | E2,E6; internals partial |
| CurrencyRepository | Maps currency code to local currency model/default when salary has no explicit currency | E2,E6,E9; dictionary not enumerated |
| CountryHostSource | Reads current country when URI host is absent/empty | E2,E6; provider implementation UNKNOWN |
| Obfuscated c44 | d calls k() for CountryCode in fallback | E2,E6; semantics/provider UNKNOWN |
| android.net.Uri | Parses query names and values; e replaces literal plus with %20 before parse | E6,E7; edge decoding not run |
| n1i.a(), n1i.b() | Residual query exclusion and known-but-unmapped filters | E6; actual set contents UNKNOWN |
| hzg.a logger | Receives exception-like unknown-param message | E6; sink/persistence UNKNOWN |
| Search model ru.hh.applicant.core.model.search.d | Input prior state, constructor output, then copy helper d.b | E2,E6; complete deobfuscated property names not established |

## Internal state vs HTTP/API

This is a client URI-to-model adapter. The leftover encoded query is assembled locally and stored in the model. That does not prove it is sent over HTTP. Endpoint, request builder, response schema and server implementation were not traced.
