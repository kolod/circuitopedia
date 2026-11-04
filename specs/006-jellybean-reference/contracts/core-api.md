# Contract: Core API (normative)

`:modules:jellybean-parts:core`, package `io.github.kolod.circuitopedia.modules.jellybeanparts.core`.
Pure Kotlin, no `android.*` imports.

```kotlin
enum class Category { OpAmps, Comparators, Timers, BipolarTransistors, Mosfets, Diodes,
    VoltageRegulators, VoltageReferences, Logic, TemperatureSensors, PowerControllers }

@JvmInline value class PartId(val value: String)          // [a-z0-9_]+

enum class ParameterKind { … }                            // see data-model.md
data class Value(val min: String?, val typ: String?, val max: String?, val unit: String)
data class Parameter(val kind: ParameterKind, val value: Value, val condition: String? = null)

enum class Package(val pinCount: Int) { To92(3), Sot23(3), Dip8(8), Dip14(14), Do35(2), Do41(2), To126(3), To220(3) }
enum class PinFunction(val symbol: String) { … }          // see data-model.md
data class Pin(val number: Int, val function: PinFunction, val channel: Char? = null)

data class FamilyMember(val name: String, val values: Map<ParameterKind, Value>)
data class Source(val maker: String, val document: String, val revision: String)

data class PartEntry(
    val id: PartId, val name: String, val category: Category,
    val parameters: List<Parameter>,
    val packageType: Package?, val pins: List<Pin>,
    val makerVariesPinout: Boolean = false,
    val members: List<FamilyMember> = emptyList(),
    val equivalent: PartId? = null,
    val source: Source,
)

/** The 34 entries in spec FR-002 order. */
val catalog: List<PartEntry>
fun catalogByCategory(): Map<Category, List<PartEntry>>   // in Category order
fun entry(id: PartId): PartEntry?
```
