package io.github.kolod.circuitopedia.modules.resistorcolorcode

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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.BandColor
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.CELL_GRADIENT
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.Selection
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.TextOnColor
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.accepts
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.cellLabel
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.darken
import io.github.kolod.circuitopedia.modules.resistorcolorcode.core.lighten

/** Cyan ring as in the reference design; the dark hairline keeps it visible on light band colors. */
private val SelectionRing = Color(0xFF00E5FF)
private val SelectionHairline = Color.Black.copy(alpha = 0.6f)
private val CellGap = 2.dp
private const val NAME_COLUMN_WEIGHT = 1.3f

/** One row per color, one column per band; tapping a valid cell selects it for that band. */
@Composable
fun ColorTable(selection: Selection, onSelect: (index: Int, color: BandColor) -> Unit, modifier: Modifier = Modifier) {
    val layout = selection.layout
    Column(modifier, verticalArrangement = Arrangement.spacedBy(CellGap)) {
        TableRow {
            SurfaceCell(Modifier.weight(NAME_COLUMN_WEIGHT), "")
            for (i in 0 until layout.bandCount) SurfaceCell(Modifier.weight(1f), layout.headerText(i))
        }
        for (color in BandColor.entries) {
            TableRow {
                SurfaceCell(Modifier.weight(NAME_COLUMN_WEIGHT), stringResource(color.nameRes()))
                for (i in 0 until layout.bandCount) {
                    if (layout.accepts(i, color)) {
                        BandCell(
                            color = color,
                            label = layout.roles[i].cellLabel(color, LocalConfiguration.current.locales[0]),
                            description = stringResource(
                                R.string.resistor_color_code_cell_description,
                                layout.bandName(i),
                                stringResource(color.nameRes()),
                                layout.roles[i].cellLabel(color, LocalConfiguration.current.locales[0]),
                            ),
                            selected = selection.colors[i] == color,
                            onClick = { onSelect(i, color) },
                            modifier = Modifier.weight(1f),
                            tag = "rcc-cell-$i-${color.name}",
                        )
                    } else {
                        // Not a valid choice for this band: empty and invisible to screen readers.
                        Box(
                            Modifier.weight(1f).fillMaxHeight()
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .clearAndSetSemantics { testTag = "rcc-cell-$i-${color.name}" },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TableRow(content: @Composable RowScope.() -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(IntrinsicSize.Min).heightIn(min = 48.dp),
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
    Text(
        text = text,
        color = color,
        maxLines = 1,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyLarge,
        autoSize = TextAutoSize.StepBased(minFontSize = 9.sp, maxFontSize = MaterialTheme.typography.bodyLarge.fontSize),
    )
}
