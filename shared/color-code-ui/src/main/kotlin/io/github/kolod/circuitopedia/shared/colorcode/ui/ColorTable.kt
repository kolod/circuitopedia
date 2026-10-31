package io.github.kolod.circuitopedia.shared.colorcode.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.kolod.circuitopedia.shared.colorcode.BandColor
import io.github.kolod.circuitopedia.shared.colorcode.CELL_GRADIENT
import io.github.kolod.circuitopedia.shared.colorcode.TextOnColor
import io.github.kolod.circuitopedia.shared.colorcode.darken
import io.github.kolod.circuitopedia.shared.colorcode.lighten

/** One band column of the table, as plain data from the module that knows what the colors mean. */
data class TableColumn(
    /** Short header, e.g. "Band 1" or "Mult.". */
    val header: String,
    /** Band name for screen readers, e.g. "Band 1" or "Multiplier". */
    val bandName: String,
    /**
     * Cell text per selectable color; colors missing here are empty and not selectable. A label may
     * contain one line break where it would not fit a narrow column; screen readers get it without.
     */
    val labels: Map<BandColor, String>,
    val selected: BandColor,
)

/** Cyan ring as in the reference design; the dark hairline keeps it visible on light band colors. */
private val SelectionRing = Color(0xFF00E5FF)
private val SelectionHairline = Color.Black.copy(alpha = 0.6f)

// Column geometry shared by the header, the body and the leader lines of BandDrawing.
internal val CellGap = 2.dp
internal val TableSidePadding = 4.dp
internal const val NAME_COLUMN_WEIGHT = 1.3f

/** Horizontal centers of the band columns in a table row [width] px wide with [gap] px between cells. */
internal fun bandColumnCenters(width: Float, gap: Float, bandCount: Int): List<Float> {
    val unit = (width - gap * bandCount) / (NAME_COLUMN_WEIGHT + bandCount)
    val firstLeft = unit * NAME_COLUMN_WEIGHT + gap
    return List(bandCount) { firstLeft + it * (unit + gap) + unit / 2 }
}

/** The header row: an empty corner over the color names, then one header per band. Stays fixed above the body. */
@Composable
internal fun ColorTableHeader(columns: List<TableColumn>, modifier: Modifier = Modifier) {
    TableRow(modifier) {
        SurfaceCell(Modifier.weight(NAME_COLUMN_WEIGHT), "")
        for (column in columns) SurfaceCell(Modifier.weight(1f), column.header)
    }
}

/** One row per color, one column per band; tapping a selectable cell selects it for that band. */
@Composable
internal fun ColorTableBody(
    columns: List<TableColumn>,
    onSelect: (column: Int, color: BandColor) -> Unit,
    tagPrefix: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(CellGap)) {
        for (color in BandColor.entries) {
            TableRow {
                val colorName = stringResource(color.nameRes())
                SurfaceCell(Modifier.weight(NAME_COLUMN_WEIGHT), colorName)
                columns.forEachIndexed { i, column ->
                    val tag = "$tagPrefix-cell-$i-${color.name}"
                    val label = column.labels[color]
                    if (label != null) {
                        BandCell(
                            color = color,
                            label = label,
                            description = stringResource(
                                R.string.color_code_cell_description,
                                column.bandName,
                                colorName,
                                label.replace("\n", ""),
                            ),
                            selected = column.selected == color,
                            onClick = { onSelect(i, color) },
                            modifier = Modifier.weight(1f),
                            tag = tag,
                        )
                    } else {
                        // Not a valid choice for this band: empty and invisible to screen readers.
                        Box(
                            Modifier.weight(1f).fillMaxHeight()
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .clearAndSetSemantics { testTag = tag },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TableRow(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier.fillMaxWidth().height(IntrinsicSize.Min).heightIn(min = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(CellGap),
        content = content,
    )
}

@Composable
private fun SurfaceCell(modifier: Modifier, text: String) {
    Box(
        modifier.fillMaxHeight().background(MaterialTheme.colorScheme.surfaceContainerHigh).padding(horizontal = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        CellText(text, MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun BandCell(
    color: BandColor,
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
    tag: String,
) {
    val brush = Brush.verticalGradient(
        listOf(Color(lighten(color.argb, CELL_GRADIENT)), Color(color.argb), Color(darken(color.argb, CELL_GRADIENT))),
    )
    val ring = if (selected) {
        Modifier.border(3.dp, SelectionRing).padding(3.dp).border(1.dp, SelectionHairline)
    } else {
        Modifier
    }
    Box(
        modifier
            .fillMaxHeight()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .clearAndSetSemantics {
                contentDescription = description
                testTag = tag
                this.selected = selected
            }
            .then(ring)
            .background(brush)
            .padding(horizontal = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        CellText(label, if (color.textOnColor == TextOnColor.Light) Color.White else Color.Black)
    }
}

@Composable
private fun CellText(text: String, color: Color) {
    // The floor is in dp, not sp: an sp floor grows with the system font scale (18sp at 200%), and
    // then labels in narrow 6-band columns no longer fit and get cut. Shrinking beats truncating.
    val minFontSize = with(LocalDensity.current) { 8.dp.toSp() }
    Text(
        text = text,
        color = color,
        maxLines = if ('\n' in text) 2 else 1,
        lineHeight = 1.1.em,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyLarge,
        autoSize = TextAutoSize.StepBased(minFontSize = minFontSize, maxFontSize = MaterialTheme.typography.bodyLarge.fontSize),
    )
}
