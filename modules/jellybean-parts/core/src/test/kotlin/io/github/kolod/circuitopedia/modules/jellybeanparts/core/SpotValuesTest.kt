package io.github.kolod.circuitopedia.modules.jellybeanparts.core

import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.GainBandwidth
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.Hfe
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.InputOffsetVoltage
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.MaxDutyCycle
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.OscillatorFrequency
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.OutputVoltage
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.PropagationDelay
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.RdsOn
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.ReferenceVoltage
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.ResponseTime
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.ScaleFactor
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.SupplyVoltage
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.Vceo
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.Vf
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.VgsTh
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.ParameterKind.Vrrm
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.Base
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.Collector
import io.github.kolod.circuitopedia.modules.jellybeanparts.core.PinFunction.Emitter
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Spot checks against the transcribed datasheet tables (task T014). Each test names its table. They
 * guard against an accidental edit of the catalog, not against a misread datasheet (the audit does that).
 */
class SpotValuesTest {
    private fun value(id: String, kind: ParameterKind, index: Int = 0): Value =
        entry(PartId(id))!!.parameters.filter { it.kind == kind }[index].value

    private fun member(id: String, name: String, kind: ParameterKind): Value =
        entry(PartId(id))!!.members.single { it.name == name }.values.getValue(kind)

    @Test fun lm358SupplyRange_SLOS068_5_3() =
        assertEquals(Value(min = "3", max = "30", unit = "V"), value("lm358", SupplyVoltage))

    @Test fun tl072OffsetVoltage_SLOS080_5_8() =
        assertEquals(Value(typ = "3", max = "10", unit = "mV"), value("tl072", InputOffsetVoltage))

    @Test fun lm324Bandwidth_SLOS066_6_10() =
        assertEquals(Value(typ = "1.2", unit = "MHz"), value("lm324", GainBandwidth))

    @Test fun lm393ResponseTime_SLCS005_5_11() =
        assertEquals(Value(typ = "1.3", unit = "µs"), value("lm393", ResponseTime))

    @Test fun ne555Supply_SLFS022_5_3() =
        assertEquals(Value(min = "4.5", max = "16", unit = "V"), value("ne555", SupplyVoltage))

    @Test fun twoN3904Vceo_maximumRatings() =
        assertEquals(Value(max = "40", unit = "V"), value("2n3904", Vceo))

    @Test fun bc548GainAndPinout_BC546D() {
        assertEquals(Value(min = "110", max = "800", unit = "—"), value("bc548", Hfe))
        assertEquals(listOf(Collector, Base, Emitter), entry(PartId("bc548"))!!.pins.map { it.function })
    }

    @Test fun twoN7000Mosfet_2N7000D() {
        assertEquals(Value(min = "0.8", max = "3.0", unit = "V"), value("2n7000", VgsTh))
        assertEquals(Value(max = "5.0", unit = "Ω"), value("2n7000", RdsOn))
    }

    @Test fun oneN4148Vrrm_81857_absoluteMaximum() =
        assertEquals(Value(max = "100", unit = "V"), value("1n4148", Vrrm))

    @Test fun oneN4007Vrrm_88503_maximumRatings() =
        assertEquals(Value(max = "1000", unit = "V"), member("1n4001_1n4007", "1N4007", Vrrm))

    @Test fun oneN5819ForwardVoltage_88525() =
        assertEquals(Value(max = "0.600", unit = "V"), value("1n5819", Vf))

    @Test fun lm317Reference_SLVS044_6_6() =
        assertEquals(Value(min = "1.2", typ = "1.25", max = "1.3", unit = "V"), value("lm317", ReferenceVoltage))

    @Test fun ua7805Output_SLVS056_7_5() =
        assertEquals(Value(min = "4.8", typ = "5", max = "5.2", unit = "V"), member("78xx", "7805", OutputVoltage))

    @Test fun mc7912Output_MC7900D() =
        assertEquals(Value(min = "-11.5", typ = "-12", max = "-12.5", unit = "V"), member("79xx", "7912", OutputVoltage))

    @Test fun tl431Reference_SLVS543_6_5() =
        assertEquals(Value(min = "2440", typ = "2495", max = "2550", unit = "mV"), value("tl431", ReferenceVoltage))

    @Test fun hc00Delay_SCLS181_6_7() =
        assertEquals(Value(typ = "9", max = "18", unit = "ns"), value("74hc", PropagationDelay))

    @Test fun lm35ScaleFactor_SNIS159_6_7() =
        assertEquals(Value(min = "9.8", typ = "10", max = "10.2", unit = "mV/°C"), value("lm35", ScaleFactor))

    @Test fun tmp36OutputAndPinout_TMP35_36_37_Table1() {
        assertEquals(Value(typ = "750", unit = "mV"), value("tmp36", ParameterKind.OutputAt25C))
        assertEquals(
            listOf(PinFunction.PositiveSupply, PinFunction.Output, PinFunction.Ground),
            entry(PartId("tmp36"))!!.pins.map { it.function },
        )
    }

    @Test fun mc34063Oscillator_SLLS636_7_5() =
        assertEquals(Value(min = "24", typ = "33", max = "42", unit = "kHz"), value("mc34063", OscillatorFrequency))

    @Test fun uc3844DutyCycle_SLUS223_6_5() =
        assertEquals(Value(min = "47", typ = "48", max = "50", unit = "%"), member("uc384x", "UC3844", MaxDutyCycle))
}
