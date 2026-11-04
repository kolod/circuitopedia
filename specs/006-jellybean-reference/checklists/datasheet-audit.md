# Datasheet Audit: Jellybean Parts Reference

**Purpose**: SC-003. Each entry's values and pinout were transcribed from the datasheet named
below. A second reader ticks the box after checking every value against the PDF page.
**Feature**: [spec.md](../spec.md)

How the PDFs were read: downloaded from the maker's site and converted with `pdftotext -table`
(xpdf 4.06), which keeps table columns aligned. Values are copied as printed.

Some PDFs lose the "µ" glyph in their text layer. These are Vishay, the onsemi Fairchild-format
sheets, TI SLVS044, FN3072 and ADI TMP35/TMP36/TMP37. onsemi Symbol-font sheets print µ as "m" instead. Rows whose
unit had to be read as µ are marked **(µ)**. The visual check must confirm them on the page.

| Done | Entry | Document | Revision | Tables used | Status |
|------|-------|----------|----------|-------------|--------|
| [ ] | LM358 | TI SLOS068 | SLOS068AB, Oct 2024 | 5.3 ROC; 5.7 EC LM358; Table 4-1 | transcribed |
| [ ] | LM324 | TI SLOS066 | SLOS066AE, Sep 2025 | 6.3 ROC; 6.7 EC LM324; 6.10 Operating Conditions; Table 5-1 | transcribed |
| [ ] | TL072 | TI SLOS080 | SLOS080W, Jul 2025 | 5.3 ROC; 5.8/5.9 EC TL07xC; Table 4-3 | transcribed |
| [ ] | OP07 | TI SLOS099 | SLOS099H, Mar 2023 | 6.3 ROC; 6.5 EC (OP07C); Table 5-1 | transcribed. VOS ±60 µV and IB ±1.8 nA are printed in the TYP column only |
| [ ] | LM393 | TI SLCS005 (not SLOS031 as in research R2) | SLCS005AH, Apr 2025 | 5.2 ROC; 5.8 EC LM393 column; 5.11 Switching | transcribed |
| [ ] | LM339 | TI SLCS006 | SLCS006Z, May 2025 | 6.5 ROC non-B; 6.10 EC LMx39; 6.14 Switching; Figure 5-1 | transcribed |
| [ ] | NE555 | TI SLFS022 | SLFS022K, Mar 2026 | 5.3 ROC; 5.5 EC; pin table | transcribed. This revision no longer lists the initial timing error |
| [ ] | 2N3904 | onsemi 2N3904/D | Rev. 3, Oct 2024 | Maximum Ratings; Thermal; EC | transcribed |
| [ ] | 2N3906 | onsemi 2N3906/D (Fairchild format) | Rev. 1.2.2 | Absolute Maximum; Thermal (2N3906 column); EC | transcribed |
| [ ] | 2N2222 (PN2222A) | onsemi PN2222A/D (Fairchild format) | Rev. 1.1.0 | Absolute Maximum; Thermal; EC | transcribed |
| [ ] | PN2907 (PN2907A) | onsemi PN2907A/D | Rev. 3, May 2007 | Maximum Ratings; EC; TO-92 Style 1 | transcribed |
| [ ] | BC548 | onsemi BC546/D | Rev. 8, Aug 2021 | Maximum Ratings; EC BC548 rows; TO-92 Style 17 (C-B-E) | transcribed |
| [ ] | BD139 | onsemi BD135/D (ST was unreachable, so research R2 was changed) | Rev. 18, Jun 2024 | Maximum Ratings; EC BD139G; TO-225 Style 1 | transcribed. TO-225 has the TO-126 outline. No f_T in this sheet |
| [ ] | BD140 | onsemi BD136/D (as for BD139) | Rev. 2, May 2024 | Absolute Maximum; EC BD140; TO-126 | transcribed. hFE is split into the -10 and -16 grades |
| [ ] | 2N7000 | onsemi 2N7000/D | Rev. 8, Apr 2011 | Maximum Ratings; EC; TO-92 Style 22 (S-G-D) | transcribed |
| [ ] | 2N7002 | onsemi 2N7002L/D (Nexperia refused the download) | Rev. 11, Jun 2024 | Maximum Ratings; Thermal; EC; SOT-23 Style 21 (G-S-D) | transcribed |
| [ ] | 1N4148 | Vishay 81857 | Rev. 1.6, 07-Nov-2024 | Absolute Maximum; EC | transcribed. IR is taken at 20 V (25 nA), because the 75 V row lost its µ |
| [ ] | 1N914 | onsemi 1N914/D | Rev. 6, Sep 2024 | Absolute Maximum; Thermal; EC | transcribed. **(µ)** IR 0.025 µA |
| [ ] | 1N4001–1N4007 | Vishay 88503 | 29-Apr-2020 | Maximum Ratings; EC | transcribed. **(µ)** IR 5.0 µA |
| [ ] | 1N5817 | Vishay 88525 | 01-Jul-2020 | Maximum Ratings; EC | transcribed. The VF test current is printed as "1.0" and "3.1", read as A |
| [ ] | 1N5819 | Vishay 88525 | 01-Jul-2020 | as 1N5817 | transcribed |
| [ ] | BAT54 | onsemi BAT54LT1/D (Nexperia refused the download) | Rev. 18, Nov 2024 | Maximum Ratings; EC; SOT-23 Style 8 (A-NC-K) | transcribed. **(µ)** IR 0.2/2.0 µA |
| [ ] | SD103 | Vishay 85754 | Rev. 2.2, 08-Nov-2024 | Absolute Maximum; EC | transcribed. **(µ)** IR 5 µA |
| [ ] | 78xx | TI SLVS056 | SLVS056P, Jan 2015 | 7.3 ROC; 7.5–7.10 EC; Pin Functions | transcribed. Members follow the sheet: 7805, 7808, 7810, 7812, 7815, 7824 (no 7809) |
| [ ] | 79xx | onsemi MC7900/D | Rev. 21, Oct 2021 | Maximum Ratings; EC MC7905C, MC7912C, MC7915C, MC7924C | transcribed |
| [ ] | LM317 | TI SLVS044 | SLVS044Z, Apr 2025 | 6.3 ROC; 6.6 EC; Pin Functions KCS | transcribed. **(µ)** I_ADJ 50/100 µA |
| [ ] | LM337 | TI SLVS047 | SLVS047L, Jan 2015 | 7.3 ROC; 7.5 EC LM337 column; Pin Functions | transcribed. **(µ)** I_ADJ 65/100 µA |
| [ ] | TL431 | TI SLVS543 | SLVS543S, May 2024 | 6.4 ROC; 6.5 EC TL431C; Table 5-1 (LP) | transcribed. **(µ)** Iref 2/4 µA. Maker-varying TO-92 pinout still to be confirmed against a second maker |
| [ ] | 74HC family | TI SCLS181 (SN74HC00) | SCLS181H, Aug 2021 | 6.3 ROC; 6.5 EC commercial; 6.7 Switching commercial | transcribed |
| [ ] | LM35 | TI SNIS159 | SNIS159H, Dec 2017 | 6.3 ROC; 6.7 EC LM35 limits; Pin Functions | transcribed |
| [ ] | TMP36 | ADI TMP35/TMP36/TMP37 (PDF supplied by the user; analog.com timed out from here) | Rev. E, Aug 2008 | Table 1 Specifications (F grade); Figure 4 T-3 (TO-92) | transcribed. **(µ)** ISY 50 µA. The text layer also drops ± and °, so the accuracy rows must be checked on the page |
| [ ] | MC34063 | TI SLLS636 | SLLS636N, Jan 2015 | 7.3 ROC; 7.5–7.8 EC; Pin Functions | transcribed |
| [ ] | UC3842–UC3845 | TI SLUS223 | SLUS223H, Oct 2024 | 4 Device Comparison; 6.3 ROC; 6.5 EC; Table 5-1 | transcribed |
| [ ] | ICL7660 | Renesas FN3072 | Rev. 7.00, Oct 5, 2010 | Electrical Specifications ICL7660; Pinouts | transcribed. **(µ)** I+ 170/500 µA |
