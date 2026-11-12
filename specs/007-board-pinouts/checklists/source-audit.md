# Source Audit: Development Board Pinouts

**Purpose**: SC-004. Before release, every board's pins, functions, placements and limits are
checked against the documents they were copied from.
**Feature**: [spec.md](../spec.md)

How the data was transcribed:
- **Copied as printed.** Pins, labels, functions and values were copied as printed.
- **Rendered pages for tables.** Where pdftotext merged table cells (RM0008 remap tables, the Uno,
  Nano, Blue Pill and WeAct schematics, the Raspberry Pi pinout figures), the page was rendered and
  read as an image.
- **Status.** A line marked "transcribed" is in the data. It becomes "audited" after a second,
  independent check (T036).

| Board | Document | Revision checked | Tables / sections used | Status |
|-------|----------|------------------|------------------------|--------|
| Uno R3 | Arduino A000066 User Manual | Rev. 4, 20/10/2026 | §2.2, §5.1, §5.2 | transcribed |
| Uno R3 | Arduino UNO-TH_Rev3e schematic | 06/03/2019 | POWER, AD, IOL, IOH and ICSP headers; D0/D1 to 16U2; L LED buffer | transcribed |
| Uno R3, Nano | Microchip DS40002061B (ATmega328P) | Rev. B, 09/2020 | Tables 14-3, 14-6, 14-9; §1.1.9; §29.1; Table 29-15; Table 30-1 notes | transcribed |
| Nano | Arduino NanoV3.3 schematic | 23/01/2019 | J1, J2, ICSP J4; FT232RL; L LED | transcribed |
| Nano | Arduino A000005 User Manual | Rev. 5, 20/10/2026 | §5 (incomplete: no AREF or D13, a repeated number, so the schematic is followed) | transcribed |
| Nano | TI SNOS412Q (LM1117) | Rev. Q, 01/2023 | §7.3 input voltage, for VIN | transcribed |
| Pico W | Raspberry Pi Pico W Datasheet | Release 7, 03/07/2026 | Figure 2, §2.1 | transcribed |
| Pico W | Raspberry Pi Pico Datasheet | Release 21, 03/07/2026 | §2.1 (variant note) | transcribed |
| Pico W | RP2040 Datasheet | build 3184e62, 2025-02-20 | Table 279; §2.9.5; Tables 622, 625 | transcribed |
| Pico 2 W | Raspberry Pi Pico 2 W Datasheet | Release 2, 03/07/2026 | Figure 2, §2.1 | transcribed |
| Pico 2 W | Raspberry Pi Pico 2 Datasheet | Release 5, 03/07/2026 | §2.1 (variant note) | transcribed |
| Pico 2 W | RP2350 Datasheet | Release 8, 01/10/2026 | Table 677; Tables 1673, 1674, 1680, 1683; §6.1.5; D.5.1 (E9, A2 only) | transcribed |
| Pi 4 B | Raspberry Pi 4 Model B Datasheet | Release 1.1, 2024 | Figure 3; Tables 3, 5; §4.1 | transcribed |
| Pi 4 B | Raspberry Pi docs, "GPIO on Raspberry Pi" | commit 0fb4163, 2026-10-29 | 3.3V, not 5V tolerant; GP2/GP3 1.8kΩ; 16mA per pin, 50mA total | transcribed |
| Pi 4 B | raspberrypi/firmware boot/overlays/README | fetched 2026-11-12 | i2c0/1/3-6, spi0-6, uart0-5, pwm, disable-bt | transcribed |
| Blue Pill | vcc-gnd schematic (via stm32-base.org) | 2016-01-26 | P2, P3, P4; LED, crystal, USB D+ resistor, BOOT jumpers | transcribed |
| Blue Pill | stm32-base.org board page | fetched 2026-11-12 | 5V rail and USB D+ resistor warnings | transcribed |
| Blue Pill | ST DS5319 | Rev 20, 28-Jul-2025 | Table 5 (LQFP48, FT column, notes 5 and 9); Tables 7, 9 | transcribed |
| Blue Pill | ST RM0008 | Rev 21, 2021 | Tables 34, 43-46, 52-56; §23.2 | transcribed |
| Blue Pill | ST ES096 | Rev 15, 30-Mar-2022 | §2.3.1-2.3.10 | transcribed |
| Black Pill | WeAct MiniF4x1Cx V3.1 schematic and board shape | V3.1, 2021-04-17 | P1, P2, P3; KEY, LED, crystal, USB, R3 on PB2, flash footprint | transcribed |
| Black Pill | ST DS10314 (WeAct copy) | DocID026289 Rev 7, 14-Dec-2017 | Table 8 (UFQFPN48, notes 2 and 4); Table 9; Tables 12, 14 | transcribed |
| Black Pill | ST DS10086 (WeAct copy) | DocID025644 Rev 3 | F401 variant note | transcribed |

## Notes

- **ST documents:** st.com did not respond to the fetch tools. The user supplied DS5319, RM0008 and
  ES096. For DS10314 and DS10086, the user approved the copies in WeAct's repository; these are
  older revisions than ST's current ones and should be rechecked against them when possible.
- **Stricter value on conflict:** for the Nano, the A000005 pin lists conflict with the schematic,
  so the schematic is followed (research R1, "Conflicts").
