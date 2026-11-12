# Changelog

All notable changes to Circuitopedia are listed here. Versions follow
[Semantic Versioning](https://semver.org/).

## [Unreleased]

### Reference

- **Board pinouts**: headers of the Arduino Uno R3 and Nano, Raspberry Pi Pico W and Pico 2 W,
  Raspberry Pi 4 Model B, STM32F103 "Blue Pill" and WeAct STM32F411 "Black Pill", drawn and as a
  list. Each pin shows its alternate functions and limits: 5 V tolerance, analog, onboard parts
  (LED, button, crystal, USB), boot role, current limits and errata, all cited from the makers'
  documents. A planner assigns UART, I2C, SPI, PWM, ADC, CAN, I2S and USB to pins and refuses
  combinations the chip cannot route, such as a second I2C on pins already in use or a remap that
  moves a whole group of pins. An optional switch keeps the screen on while a board is open.

## [0.1.0] - 2026-10-31

The first release. Circuitopedia is a free, ad-free and offline reference app for electronics
engineers and hobbyists. It needs no permissions and runs on Android 8.0 (API 26) and newer,
in portrait orientation.

### App

- Catalog with two tabs, Calculations and Reference. Entries are sorted by their localized
  title. The tab and the open screen survive rotation and process recreation.
- Available in English, German, French, Spanish, Japanese and Ukrainian. Numbers use the decimal
  separator of the selected language.
- Supports the light and dark system themes, 200% font size and screen readers.

### Calculators

- **Resistor color code**: reads 3-, 4-, 5- and 6-band resistors (IEC 60062:2016). It has a
  color table and a resistor drawing whose bands are joined to their columns.
- **Inductor color code**: reads 3- and 4-band molded inductors in nH, µH and mH. Gold in the
  first or second band is read as the decimal point (MIL marking), as in Bourns' MIL/EIA marking
  sheets.
- **Resistor combinations**: matches a target value with one to three standard resistors of the
  E3 to E192 series (IEC 60063:2015), in series, in parallel, or two in parallel plus one in
  series. Results are sorted by deviation, with the five best of each kind.
- **Voltage divider**: solves U1, U2, R1, R2 or R from any three of them. It then builds the
  divider from standard resistors, choosing both arms together for the smallest output-voltage
  error, with the total resistance kept within ±10%.

[0.1.0]: https://github.com/kolod/circuitopedia/releases/tag/v0.1.0
