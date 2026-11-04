package io.github.kolod.circuitopedia.modules.jellybeanparts

import androidx.annotation.StringRes
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Category
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.Package
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PartId
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction

/** The localized one-line purpose and the description of a catalog entry. */
internal data class PartTexts(@param:StringRes val purpose: Int, @param:StringRes val description: Int)

private val PART_TEXTS: Map<String, PartTexts> = mapOf(
    "lm358" to PartTexts(R.string.jellybean_parts_lm358_purpose, R.string.jellybean_parts_lm358_description),
    "lm324" to PartTexts(R.string.jellybean_parts_lm324_purpose, R.string.jellybean_parts_lm324_description),
    "tl072" to PartTexts(R.string.jellybean_parts_tl072_purpose, R.string.jellybean_parts_tl072_description),
    "op07" to PartTexts(R.string.jellybean_parts_op07_purpose, R.string.jellybean_parts_op07_description),
    "lm393" to PartTexts(R.string.jellybean_parts_lm393_purpose, R.string.jellybean_parts_lm393_description),
    "lm339" to PartTexts(R.string.jellybean_parts_lm339_purpose, R.string.jellybean_parts_lm339_description),
    "ne555" to PartTexts(R.string.jellybean_parts_ne555_purpose, R.string.jellybean_parts_ne555_description),
    "2n3904" to PartTexts(R.string.jellybean_parts_2n3904_purpose, R.string.jellybean_parts_2n3904_description),
    "2n3906" to PartTexts(R.string.jellybean_parts_2n3906_purpose, R.string.jellybean_parts_2n3906_description),
    "pn2222a" to PartTexts(R.string.jellybean_parts_pn2222a_purpose, R.string.jellybean_parts_pn2222a_description),
    "pn2907a" to PartTexts(R.string.jellybean_parts_pn2907a_purpose, R.string.jellybean_parts_pn2907a_description),
    "bc548" to PartTexts(R.string.jellybean_parts_bc548_purpose, R.string.jellybean_parts_bc548_description),
    "bd139" to PartTexts(R.string.jellybean_parts_bd139_purpose, R.string.jellybean_parts_bd139_description),
    "bd140" to PartTexts(R.string.jellybean_parts_bd140_purpose, R.string.jellybean_parts_bd140_description),
    "2n7000" to PartTexts(R.string.jellybean_parts_2n7000_purpose, R.string.jellybean_parts_2n7000_description),
    "2n7002" to PartTexts(R.string.jellybean_parts_2n7002_purpose, R.string.jellybean_parts_2n7002_description),
    "1n4148" to PartTexts(R.string.jellybean_parts_1n4148_purpose, R.string.jellybean_parts_1n4148_description),
    "1n914" to PartTexts(R.string.jellybean_parts_1n914_purpose, R.string.jellybean_parts_1n914_description),
    "1n4001_1n4007" to PartTexts(R.string.jellybean_parts_1n4001_1n4007_purpose, R.string.jellybean_parts_1n4001_1n4007_description),
    "1n5817" to PartTexts(R.string.jellybean_parts_1n5817_purpose, R.string.jellybean_parts_1n5817_description),
    "1n5819" to PartTexts(R.string.jellybean_parts_1n5819_purpose, R.string.jellybean_parts_1n5819_description),
    "bat54" to PartTexts(R.string.jellybean_parts_bat54_purpose, R.string.jellybean_parts_bat54_description),
    "sd103" to PartTexts(R.string.jellybean_parts_sd103_purpose, R.string.jellybean_parts_sd103_description),
    "78xx" to PartTexts(R.string.jellybean_parts_78xx_purpose, R.string.jellybean_parts_78xx_description),
    "79xx" to PartTexts(R.string.jellybean_parts_79xx_purpose, R.string.jellybean_parts_79xx_description),
    "lm317" to PartTexts(R.string.jellybean_parts_lm317_purpose, R.string.jellybean_parts_lm317_description),
    "lm337" to PartTexts(R.string.jellybean_parts_lm337_purpose, R.string.jellybean_parts_lm337_description),
    "tl431" to PartTexts(R.string.jellybean_parts_tl431_purpose, R.string.jellybean_parts_tl431_description),
    "74hc" to PartTexts(R.string.jellybean_parts_74hc_purpose, R.string.jellybean_parts_74hc_description),
    "lm35" to PartTexts(R.string.jellybean_parts_lm35_purpose, R.string.jellybean_parts_lm35_description),
    "tmp36" to PartTexts(R.string.jellybean_parts_tmp36_purpose, R.string.jellybean_parts_tmp36_description),
    "mc34063" to PartTexts(R.string.jellybean_parts_mc34063_purpose, R.string.jellybean_parts_mc34063_description),
    "uc384x" to PartTexts(R.string.jellybean_parts_uc384x_purpose, R.string.jellybean_parts_uc384x_description),
    "icl7660" to PartTexts(R.string.jellybean_parts_icl7660_purpose, R.string.jellybean_parts_icl7660_description),
)

/** The texts of [id]; every catalog id has them (checked by PartsScreenTest). */
internal fun partTexts(id: PartId): PartTexts = PART_TEXTS.getValue(id.value)

@StringRes
internal fun Category.label(): Int = when (this) {
    Category.OpAmps -> R.string.jellybean_parts_category_opamps
    Category.Comparators -> R.string.jellybean_parts_category_comparators
    Category.Timers -> R.string.jellybean_parts_category_timers
    Category.BipolarTransistors -> R.string.jellybean_parts_category_bipolartransistors
    Category.Mosfets -> R.string.jellybean_parts_category_mosfets
    Category.Diodes -> R.string.jellybean_parts_category_diodes
    Category.VoltageRegulators -> R.string.jellybean_parts_category_voltageregulators
    Category.VoltageReferences -> R.string.jellybean_parts_category_voltagereferences
    Category.Logic -> R.string.jellybean_parts_category_logic
    Category.TemperatureSensors -> R.string.jellybean_parts_category_temperaturesensors
    Category.PowerControllers -> R.string.jellybean_parts_category_powercontrollers
}

/** The view note of a package drawing, which starts with the package name. */
@StringRes
internal fun Package.view(): Int = when (this) {
    Package.To92 -> R.string.jellybean_parts_view_to92
    Package.Sot23 -> R.string.jellybean_parts_view_sot23
    Package.Dip8 -> R.string.jellybean_parts_view_dip8
    Package.Dip14 -> R.string.jellybean_parts_view_dip14
    Package.Do35 -> R.string.jellybean_parts_view_do35
    Package.Do41 -> R.string.jellybean_parts_view_do41
    Package.To126 -> R.string.jellybean_parts_view_to126
    Package.To220 -> R.string.jellybean_parts_view_to220
}

@StringRes
internal fun ParameterKind.label(): Int = when (this) {
    ParameterKind.SupplyVoltage -> R.string.jellybean_parts_param_supplyvoltage
    ParameterKind.SupplyCurrent -> R.string.jellybean_parts_param_supplycurrent
    ParameterKind.PowerDissipationTotal -> R.string.jellybean_parts_param_powerdissipationtotal
    ParameterKind.InputOffsetVoltage -> R.string.jellybean_parts_param_inputoffsetvoltage
    ParameterKind.InputBiasCurrent -> R.string.jellybean_parts_param_inputbiascurrent
    ParameterKind.GainBandwidth -> R.string.jellybean_parts_param_gainbandwidth
    ParameterKind.SlewRate -> R.string.jellybean_parts_param_slewrate
    ParameterKind.ResponseTime -> R.string.jellybean_parts_param_responsetime
    ParameterKind.OutputSinkCurrent -> R.string.jellybean_parts_param_outputsinkcurrent
    ParameterKind.OutputCurrent -> R.string.jellybean_parts_param_outputcurrent
    ParameterKind.OutputLowVoltage -> R.string.jellybean_parts_param_outputlowvoltage
    ParameterKind.OutputHighVoltage -> R.string.jellybean_parts_param_outputhighvoltage
    ParameterKind.PropagationDelay -> R.string.jellybean_parts_param_propagationdelay
    ParameterKind.ScaleFactor -> R.string.jellybean_parts_param_scalefactor
    ParameterKind.OutputAt25C -> R.string.jellybean_parts_param_outputat25c
    ParameterKind.Accuracy -> R.string.jellybean_parts_param_accuracy
    ParameterKind.OperatingTemperature -> R.string.jellybean_parts_param_operatingtemperature
    ParameterKind.OscillatorFrequency -> R.string.jellybean_parts_param_oscillatorfrequency
    ParameterKind.SwitchCurrent -> R.string.jellybean_parts_param_switchcurrent
    ParameterKind.UvloOn -> R.string.jellybean_parts_param_uvloon
    ParameterKind.UvloOff -> R.string.jellybean_parts_param_uvlooff
    ParameterKind.MaxDutyCycle -> R.string.jellybean_parts_param_maxdutycycle
    ParameterKind.PowerEfficiency -> R.string.jellybean_parts_param_powerefficiency
    ParameterKind.ConversionEfficiency -> R.string.jellybean_parts_param_conversionefficiency
    ParameterKind.OutputResistance -> R.string.jellybean_parts_param_outputresistance
    ParameterKind.Vceo -> R.string.jellybean_parts_param_vceo
    ParameterKind.Ic -> R.string.jellybean_parts_param_ic
    ParameterKind.Pd -> R.string.jellybean_parts_param_pd
    ParameterKind.Hfe -> R.string.jellybean_parts_param_hfe
    ParameterKind.Ft -> R.string.jellybean_parts_param_ft
    ParameterKind.VceSat -> R.string.jellybean_parts_param_vcesat
    ParameterKind.Vds -> R.string.jellybean_parts_param_vds
    ParameterKind.Id -> R.string.jellybean_parts_param_id
    ParameterKind.RdsOn -> R.string.jellybean_parts_param_rdson
    ParameterKind.VgsTh -> R.string.jellybean_parts_param_vgsth
    ParameterKind.Vrrm -> R.string.jellybean_parts_param_vrrm
    ParameterKind.IfAv -> R.string.jellybean_parts_param_ifav
    ParameterKind.Ifsm -> R.string.jellybean_parts_param_ifsm
    ParameterKind.Vf -> R.string.jellybean_parts_param_vf
    ParameterKind.Trr -> R.string.jellybean_parts_param_trr
    ParameterKind.Ir -> R.string.jellybean_parts_param_ir
    ParameterKind.OutputVoltage -> R.string.jellybean_parts_param_outputvoltage
    ParameterKind.InputVoltage -> R.string.jellybean_parts_param_inputvoltage
    ParameterKind.Dropout -> R.string.jellybean_parts_param_dropout
    ParameterKind.ReferenceVoltage -> R.string.jellybean_parts_param_referencevoltage
    ParameterKind.AdjustCurrent -> R.string.jellybean_parts_param_adjustcurrent
    ParameterKind.CathodeVoltage -> R.string.jellybean_parts_param_cathodevoltage
    ParameterKind.CathodeCurrent -> R.string.jellybean_parts_param_cathodecurrent
    ParameterKind.MinCathodeCurrent -> R.string.jellybean_parts_param_mincathodecurrent
}

@StringRes
internal fun PinFunction.label(): Int = when (this) {
    PinFunction.Collector -> R.string.jellybean_parts_pin_collector
    PinFunction.Base -> R.string.jellybean_parts_pin_base
    PinFunction.Emitter -> R.string.jellybean_parts_pin_emitter
    PinFunction.Gate -> R.string.jellybean_parts_pin_gate
    PinFunction.Drain -> R.string.jellybean_parts_pin_drain
    PinFunction.Source -> R.string.jellybean_parts_pin_source
    PinFunction.Anode -> R.string.jellybean_parts_pin_anode
    PinFunction.Cathode -> R.string.jellybean_parts_pin_cathode
    PinFunction.Output -> R.string.jellybean_parts_pin_output
    PinFunction.InvertingInput -> R.string.jellybean_parts_pin_invertinginput
    PinFunction.NonInvertingInput -> R.string.jellybean_parts_pin_noninvertinginput
    PinFunction.PositiveSupply -> R.string.jellybean_parts_pin_positivesupply
    PinFunction.NegativeSupply -> R.string.jellybean_parts_pin_negativesupply
    PinFunction.Ground -> R.string.jellybean_parts_pin_ground
    PinFunction.Input -> R.string.jellybean_parts_pin_input
    PinFunction.Adjust -> R.string.jellybean_parts_pin_adjust
    PinFunction.Reference -> R.string.jellybean_parts_pin_reference
    PinFunction.Trigger -> R.string.jellybean_parts_pin_trigger
    PinFunction.Threshold -> R.string.jellybean_parts_pin_threshold
    PinFunction.Discharge -> R.string.jellybean_parts_pin_discharge
    PinFunction.ControlVoltage -> R.string.jellybean_parts_pin_controlvoltage
    PinFunction.Reset -> R.string.jellybean_parts_pin_reset
    PinFunction.NoConnect -> R.string.jellybean_parts_pin_noconnect
    PinFunction.OffsetNull -> R.string.jellybean_parts_pin_offsetnull
    PinFunction.Compensation -> R.string.jellybean_parts_pin_compensation
    PinFunction.VoltageFeedback -> R.string.jellybean_parts_pin_voltagefeedback
    PinFunction.CurrentSense -> R.string.jellybean_parts_pin_currentsense
    PinFunction.RtCt -> R.string.jellybean_parts_pin_rtct
    PinFunction.ReferenceOutput -> R.string.jellybean_parts_pin_referenceoutput
    PinFunction.CapPlus -> R.string.jellybean_parts_pin_capplus
    PinFunction.CapMinus -> R.string.jellybean_parts_pin_capminus
    PinFunction.Oscillator -> R.string.jellybean_parts_pin_oscillator
    PinFunction.LowVoltage -> R.string.jellybean_parts_pin_lowvoltage
    PinFunction.SwitchCollector -> R.string.jellybean_parts_pin_switchcollector
    PinFunction.SwitchEmitter -> R.string.jellybean_parts_pin_switchemitter
    PinFunction.TimingCapacitor -> R.string.jellybean_parts_pin_timingcapacitor
    PinFunction.DriverCollector -> R.string.jellybean_parts_pin_drivercollector
    PinFunction.PeakCurrentSense -> R.string.jellybean_parts_pin_peakcurrentsense
    PinFunction.ComparatorInvertingInput -> R.string.jellybean_parts_pin_comparatorinvertinginput
}

/** Spoken words for a printed unit, or null for a unitless value ("—"). */
@StringRes
internal fun unitWords(unit: String): Int? = when (unit) {
    "V" -> R.string.jellybean_parts_unit_v
    "mV" -> R.string.jellybean_parts_unit_mv
    "A" -> R.string.jellybean_parts_unit_a
    "mA" -> R.string.jellybean_parts_unit_ma
    "µA" -> R.string.jellybean_parts_unit_ua
    "nA" -> R.string.jellybean_parts_unit_na
    "W" -> R.string.jellybean_parts_unit_w
    "mW" -> R.string.jellybean_parts_unit_mw
    "MHz" -> R.string.jellybean_parts_unit_mhz
    "kHz" -> R.string.jellybean_parts_unit_khz
    "Ω" -> R.string.jellybean_parts_unit_ohm
    "°C" -> R.string.jellybean_parts_unit_degc
    "ns" -> R.string.jellybean_parts_unit_ns
    "µs" -> R.string.jellybean_parts_unit_us
    "V/µs" -> R.string.jellybean_parts_unit_v_per_us
    "mV/°C" -> R.string.jellybean_parts_unit_mv_per_degc
    "%" -> R.string.jellybean_parts_unit_percent
    else -> null
}
