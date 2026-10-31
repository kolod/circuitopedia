# Contract: Shared Compose UI (normative)

`:shared:color-code-ui`, package `io.github.kolod.circuitopedia.shared.colorcode.ui`. It knows
nothing about resistors or inductors: modules pass plain data.

```kotlin
/** One band column of the table. */
data class TableColumn(
    val header: String,                     // short, e.g. "Band 1", "Mult."
    val bandName: String,                   // for screen readers, e.g. "Multiplier"
    val labels: Map<BandColor, String>,     // cell text (may contain one "\n"); missing = not selectable
    val selected: BandColor,
    val spokenLabels: Map<BandColor, String> = emptyMap(),  // screen-reader meaning, e.g. "Decimal point"
)

/** One tab's page content (a plain class: it holds a lambda). */
class ColorCodePage(
    val value: String,                      // formatted reading, e.g. "47µH ±10%"
    val columns: List<TableColumn>,
    val drawing: @Composable (Modifier) -> Unit,   // usually BandDrawing
)

/**
 * Tabs over a swipeable pager. Each page shows value, drawing and table header fixed, and only
 * the color rows scrolling (spec 002 FR-002). [tagPrefix] keeps the existing test tags:
 * "<prefix>-tab-N", "<prefix>-pager", "<prefix>-page-N", "<prefix>-value",
 * "<prefix>-cell-<index>-<Color>" (N = the tab's band count).
 */
@Composable
fun ColorCodeScaffold(
    bandCounts: List<Int>,                  // one tab per entry, e.g. [3, 4]
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    tagPrefix: String,
    page: @Composable (tab: Int) -> ColorCodePage,
    onSelect: (tab: Int, column: Int, color: BandColor) -> Unit,
)

/**
 * The SVG drawing with bands recolored through CSS and leader lines to the table's columns
 * (spec 002 R10). [shownBands] are the SVG band numbers (1-based) for each column, left to right.
 */
@Composable
fun BandDrawing(@RawRes svg: Int, shownBands: List<Int>, colors: List<BandColor>, modifier: Modifier = Modifier)

@StringRes fun BandColor.nameRes(): Int
```

Common strings (`color_code_` prefix, six locales): `bands` (plurals "%d bands"), 12 color names,
`header_band` ("Band %d"), `header_multiplier` ("Mult."), `header_tolerance` ("Toler."),
`band_multiplier` ("Multiplier"), `band_tolerance` ("Tolerance"), `cell_description`
("%1$s, %2$s, %3$s"). Module-specific ones (the resistor's TCR, the inductor's decimal point) stay
in the modules.
