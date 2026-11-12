package io.github.kolod.circuitopedia.modules.boardpinouts

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import io.github.kolod.circuitopedia.modules.boardpinouts.core.BoardId
import io.github.kolod.circuitopedia.modules.boardpinouts.core.BoardNote
import io.github.kolod.circuitopedia.modules.boardpinouts.core.BoardUse
import io.github.kolod.circuitopedia.modules.boardpinouts.core.BootEffect
import io.github.kolod.circuitopedia.modules.boardpinouts.core.ConditionKey
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Conflict
import io.github.kolod.circuitopedia.modules.boardpinouts.core.FunctionKind
import io.github.kolod.circuitopedia.modules.boardpinouts.core.GroupKey
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Limit
import io.github.kolod.circuitopedia.modules.boardpinouts.core.LimitKind
import io.github.kolod.circuitopedia.modules.boardpinouts.core.NoteKey
import io.github.kolod.circuitopedia.modules.boardpinouts.core.PowerRole
import io.github.kolod.circuitopedia.modules.boardpinouts.core.ResourceId
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Value
import io.github.kolod.circuitopedia.modules.boardpinouts.core.ViewSide
import io.github.kolod.circuitopedia.modules.boardpinouts.core.Warning
import java.text.DecimalFormatSymbols
import java.util.Locale

// Maps the core's stable keys to string resources, and formats printed values for the locale.

@StringRes
internal fun BoardId.description(): Int = when (value) {
    "uno-r3" -> R.string.board_pinouts_board_uno_r3
    "nano" -> R.string.board_pinouts_board_nano
    "pico-w" -> R.string.board_pinouts_board_pico_w
    "pico2-w" -> R.string.board_pinouts_board_pico2_w
    "rpi4b" -> R.string.board_pinouts_board_rpi4b
    "blue-pill" -> R.string.board_pinouts_board_blue_pill
    "black-pill" -> R.string.board_pinouts_board_black_pill
    else -> error("no description for $value")
}

@StringRes
internal fun ViewSide.label(): Int = when (this) {
    ViewSide.TopUsbUp -> R.string.board_pinouts_view_topusbup
    ViewSide.TopRotatedUsbUp -> R.string.board_pinouts_view_toprotatedusbup
    ViewSide.TopRotatedUsbDown -> R.string.board_pinouts_view_toprotatedusbdown
}

@StringRes
internal fun BoardNote.label(): Int = when (this) {
    BoardNote.PicoVariant -> R.string.board_pinouts_bnote_picovariant
    BoardNote.Pico2Variant -> R.string.board_pinouts_bnote_pico2variant
    BoardNote.F401Variant -> R.string.board_pinouts_bnote_f401variant
    BoardNote.ReferenceSchematic -> R.string.board_pinouts_bnote_referenceschematic
    BoardNote.ClonesMayDiffer -> R.string.board_pinouts_bnote_clonesmaydiffer
    BoardNote.NoFiveVoltGpio -> R.string.board_pinouts_bnote_nofivevoltgpio
    BoardNote.UsbPullupValue -> R.string.board_pinouts_bnote_usbpullupvalue
    BoardNote.FiveVoltRailUnprotected -> R.string.board_pinouts_bnote_fivevoltrailunprotected
}

@StringRes
internal fun LimitKind.label(): Int = when (this) {
    LimitKind.FiveVoltTolerant -> R.string.board_pinouts_limit_fivevolttolerant
    LimitKind.ThreeVoltOnly -> R.string.board_pinouts_limit_threevoltonly
    LimitKind.Analog -> R.string.board_pinouts_limit_analog
    LimitKind.BoardUse -> R.string.board_pinouts_limit_boarduse
    LimitKind.InputOnly -> R.string.board_pinouts_limit_inputonly
    LimitKind.OpenDrain -> R.string.board_pinouts_limit_opendrain
    LimitKind.BoardPull -> R.string.board_pinouts_limit_boardpull
    LimitKind.BootRole -> R.string.board_pinouts_limit_bootrole
    LimitKind.CurrentLimit -> R.string.board_pinouts_limit_currentlimit
    LimitKind.Erratum -> R.string.board_pinouts_limit_erratum
}

/** A short symbol per limit, so marks read without color (FR-016). */
internal fun LimitKind.symbol(): String = when (this) {
    LimitKind.FiveVoltTolerant -> "5V"
    LimitKind.ThreeVoltOnly -> "3V3"
    LimitKind.Analog -> "~"
    LimitKind.BoardUse -> "●"
    LimitKind.InputOnly -> "→"
    LimitKind.OpenDrain -> "⊥"
    LimitKind.BoardPull -> "R"
    LimitKind.BootRole -> "⏻"
    LimitKind.CurrentLimit -> "mA"
    LimitKind.Erratum -> "!"
}

/** Mark colors; each mark also has its symbol, so color is never the only cue. */
internal fun LimitKind.color(dark: Boolean): Color = when (this) {
    LimitKind.FiveVoltTolerant -> if (dark) Color(0xFF81C784) else Color(0xFF2E7D32)
    LimitKind.ThreeVoltOnly -> if (dark) Color(0xFFFFB74D) else Color(0xFFE65100)
    LimitKind.Analog -> if (dark) Color(0xFF64B5F6) else Color(0xFF1565C0)
    LimitKind.BoardUse -> if (dark) Color(0xFFBA68C8) else Color(0xFF6A1B9A)
    LimitKind.InputOnly, LimitKind.OpenDrain, LimitKind.BoardPull -> if (dark) Color(0xFF4DD0E1) else Color(0xFF00838F)
    LimitKind.BootRole -> if (dark) Color(0xFFE57373) else Color(0xFFC62828)
    LimitKind.CurrentLimit -> if (dark) Color(0xFFBDBDBD) else Color(0xFF616161)
    LimitKind.Erratum -> if (dark) Color(0xFFFFF176) else Color(0xFF9E7700)
}

@StringRes
internal fun BoardUse.label(): Int = when (this) {
    BoardUse.Usb -> R.string.board_pinouts_use_usb
    BoardUse.UsbSerial -> R.string.board_pinouts_use_usbserial
    BoardUse.Crystal -> R.string.board_pinouts_use_crystal
    BoardUse.Led -> R.string.board_pinouts_use_led
    BoardUse.Button -> R.string.board_pinouts_use_button
    BoardUse.Debug -> R.string.board_pinouts_use_debug
    BoardUse.IdEeprom -> R.string.board_pinouts_use_ideeprom
    BoardUse.Flash -> R.string.board_pinouts_use_flash
}

/** The short tag drawn next to a pin wired to this onboard part ("LED", "button"). */
@StringRes
internal fun BoardUse.tag(): Int = when (this) {
    BoardUse.Usb -> R.string.board_pinouts_tag_usb
    BoardUse.UsbSerial -> R.string.board_pinouts_tag_usbserial
    BoardUse.Crystal -> R.string.board_pinouts_tag_crystal
    BoardUse.Led -> R.string.board_pinouts_tag_led
    BoardUse.Button -> R.string.board_pinouts_tag_button
    BoardUse.Debug -> R.string.board_pinouts_tag_debug
    BoardUse.IdEeprom -> R.string.board_pinouts_tag_ideeprom
    BoardUse.Flash -> R.string.board_pinouts_tag_flash
}

@StringRes
internal fun BootEffect.label(): Int = when (this) {
    BootEffect.HatEepromProbe -> R.string.board_pinouts_boot_hateepromprobe
    BootEffect.BootMemorySelect -> R.string.board_pinouts_boot_bootmemoryselect
    BootEffect.DebugAfterReset -> R.string.board_pinouts_boot_debugafterreset
}

@StringRes
internal fun ConditionKey.label(): Int = when (this) {
    ConditionKey.NotAnalogMode -> R.string.board_pinouts_condition_notanalogmode
    ConditionKey.IovddPowered -> R.string.board_pinouts_condition_iovddpowered
    ConditionKey.PullUp -> R.string.board_pinouts_condition_pullup
    ConditionKey.PullDown -> R.string.board_pinouts_condition_pulldown
    ConditionKey.AbsoluteMaximum -> R.string.board_pinouts_condition_absolutemaximum
    ConditionKey.UpToAvcc -> R.string.board_pinouts_condition_uptoavcc
    ConditionKey.ConnectedTo5V -> R.string.board_pinouts_condition_connectedto5v
    ConditionKey.Nominal -> R.string.board_pinouts_condition_nominal
    ConditionKey.FiveVoltSupply -> R.string.board_pinouts_condition_fivevoltsupply
    ConditionKey.DriveSetting -> R.string.board_pinouts_condition_drivesetting
    ConditionKey.SinkOnly -> R.string.board_pinouts_condition_sinkonly
}

@StringRes
internal fun GroupKey.label(): Int = when (this) {
    GroupKey.SourceSum -> R.string.board_pinouts_group_sourcesum
    GroupKey.SinkSum -> R.string.board_pinouts_group_sinksum
    GroupKey.SupplyPin -> R.string.board_pinouts_group_supplypin
    GroupKey.TotalSum -> R.string.board_pinouts_group_totalsum
}

@StringRes
internal fun PowerRole.label(): Int = when (this) {
    PowerRole.Ground -> R.string.board_pinouts_power_ground
    PowerRole.NotConnected -> R.string.board_pinouts_power_notconnected
    PowerRole.Supply3V3 -> R.string.board_pinouts_power_supply3v3
    PowerRole.Supply5V -> R.string.board_pinouts_power_supply5v
    PowerRole.Vin -> R.string.board_pinouts_power_vin
    PowerRole.Vsys -> R.string.board_pinouts_power_vsys
    PowerRole.Vbus -> R.string.board_pinouts_power_vbus
    PowerRole.Vbat -> R.string.board_pinouts_power_vbat
    PowerRole.Aref -> R.string.board_pinouts_power_aref
    PowerRole.AdcVref -> R.string.board_pinouts_power_adcvref
    PowerRole.Ioref -> R.string.board_pinouts_power_ioref
    PowerRole.Reset -> R.string.board_pinouts_power_reset
    PowerRole.Run -> R.string.board_pinouts_power_run
    PowerRole.Enable -> R.string.board_pinouts_power_enable
}

@StringRes
internal fun NoteKey.label(): Int = when (this) {
    NoteKey.NeedsOverlay -> R.string.board_pinouts_note_needsoverlay
    NoteKey.BluetoothUsesUart0 -> R.string.board_pinouts_note_bluetoothusesuart0
    NoteKey.Timer0UsedByMillis -> R.string.board_pinouts_note_timer0usedbymillis
    NoteKey.SsMustBeOutput -> R.string.board_pinouts_note_ssmustbeoutput
    NoteKey.JtagMustBeReleased -> R.string.board_pinouts_note_jtagmustbereleased
    NoteKey.UsbSerialShared -> R.string.board_pinouts_note_usbserialshared
    NoteKey.AnalogAudioUsesPwm -> R.string.board_pinouts_note_analogaudiousespwm
    NoteKey.UsbPullupOnBoard -> R.string.board_pinouts_note_usbpulluponboard
}

@StringRes
internal fun FunctionKind.label(): Int = when (this) {
    FunctionKind.Uart -> R.string.board_pinouts_function_uart
    FunctionKind.I2c -> R.string.board_pinouts_function_i2c
    FunctionKind.Spi -> R.string.board_pinouts_function_spi
    FunctionKind.Pwm -> R.string.board_pinouts_function_pwm
    FunctionKind.Adc -> R.string.board_pinouts_function_adc
    FunctionKind.Can -> R.string.board_pinouts_function_can
    FunctionKind.I2s -> R.string.board_pinouts_function_i2s
    FunctionKind.Usb -> R.string.board_pinouts_function_usb
}

// Values ------------------------------------------------------------------------------------------

/** The printed digits with the locale's decimal separator; no rounding (spec, precision rules). */
internal fun printed(text: String, locale: Locale): String =
    text.replace('.', DecimalFormatSymbols.getInstance(locale).decimalSeparator)

private fun unitWord(res: Resources, unit: String): String = when (unit) {
    "V" -> res.getString(R.string.board_pinouts_unit_v)
    "mA" -> res.getString(R.string.board_pinouts_unit_ma)
    "kΩ" -> res.getString(R.string.board_pinouts_unit_kohm)
    else -> unit
}

/** "max 40,0 mA", "1,8 V to 5,5 V", "5 V"; [spoken] spells the unit out for screen readers. */
internal fun valueText(res: Resources, value: Value, locale: Locale, spoken: Boolean = false): String {
    val unit = if (spoken) unitWord(res, value.unit) else value.unit
    fun v(text: String) = "${printed(text, locale)} $unit"
    return when {
        value.min != null && value.max != null -> res.getString(R.string.board_pinouts_value_range, v(value.min!!), v(value.max!!))
        value.max != null -> res.getString(R.string.board_pinouts_value_max, v(value.max!!))
        value.min != null -> res.getString(R.string.board_pinouts_value_min, v(value.min!!))
        else -> res.getString(R.string.board_pinouts_value_typ, v(value.typ!!))
    }
}

/** One line explaining a limit, with its value, condition, use or effect. */
internal fun limitExplanation(res: Resources, limit: Limit, locale: Locale, spoken: Boolean = false): String {
    val condition = limit.condition?.let { res.getString(it.label()) }
    val base = when (limit.kind) {
        LimitKind.FiveVoltTolerant -> res.getString(R.string.board_pinouts_limit_fivevolttolerant_explain)
        LimitKind.ThreeVoltOnly -> res.getString(R.string.board_pinouts_limit_threevoltonly_explain)
        LimitKind.Analog -> res.getString(R.string.board_pinouts_limit_analog_explain)
        LimitKind.BoardUse -> res.getString(R.string.board_pinouts_limit_boarduse_explain, res.getString(limit.use!!.label()))
        LimitKind.InputOnly -> res.getString(R.string.board_pinouts_limit_inputonly_explain)
        LimitKind.OpenDrain -> res.getString(R.string.board_pinouts_limit_opendrain_explain)
        LimitKind.BoardPull -> res.getString(
            R.string.board_pinouts_limit_boardpull_explain, valueText(res, limit.value!!, locale, spoken), condition.orEmpty(),
        )
        LimitKind.BootRole -> res.getString(R.string.board_pinouts_limit_bootrole_explain, res.getString(limit.effect!!.label()))
        LimitKind.CurrentLimit -> res.getString(
            R.string.board_pinouts_limit_currentlimit_explain, valueText(res, limit.value!!, locale, spoken), condition.orEmpty(),
        )
        LimitKind.Erratum -> res.getString(R.string.board_pinouts_limit_erratum_explain, limit.erratum!!)
    }
    val extra = when (limit.kind) {
        LimitKind.FiveVoltTolerant -> listOfNotNull(limit.value?.let { valueText(res, it, locale, spoken) }, condition)
        else -> emptyList()
    }
    return if (extra.isEmpty()) base else "$base (${extra.joinToString(", ")})"
}

// Planner messages ---------------------------------------------------------------------------------

internal fun resourceText(res: Resources, resource: ResourceId): String = when {
    resource.value == "usb-can-sram" -> res.getString(R.string.board_pinouts_resource_usb_can_sram)
    resource.value.startsWith("es096-") -> res.getString(R.string.board_pinouts_resource_erratum, "ES096 " + resource.value.removePrefix("es096-"))
    resource.value.startsWith("spi") -> res.getString(R.string.board_pinouts_resource_peripheral, "SPI${resource.value.removePrefix("spi")}/I2S${resource.value.removePrefix("spi")}")
    else -> resource.value
}

/** "Uses GPIO8, already assigned to SPI0 CE0." — [instance] resolves a placement id to its instance name. */
internal fun conflictText(res: Resources, conflict: Conflict, instance: (io.github.kolod.circuitopedia.modules.boardpinouts.core.PlacementId) -> String): String =
    when (conflict) {
        is Conflict.SameInstance -> res.getString(R.string.board_pinouts_conflict_same_instance, instance(conflict.holder))
        is Conflict.PinTaken -> res.getString(R.string.board_pinouts_conflict_pin_taken, conflict.pin.name, instance(conflict.holder), conflict.signal)
        is Conflict.ResourceTaken -> res.getString(
            R.string.board_pinouts_conflict_resource_taken, resourceText(res, conflict.resource), instance(conflict.holder),
        )
    }

internal fun warningText(res: Resources, warning: Warning): String = when (warning) {
    is Warning.PinBoardUse -> res.getString(R.string.board_pinouts_warning_pin, warning.pin.name, res.getString(warning.use.label()))
    is Warning.PinBootRole -> res.getString(R.string.board_pinouts_warning_pin, warning.pin.name, res.getString(warning.effect.label()))
}
