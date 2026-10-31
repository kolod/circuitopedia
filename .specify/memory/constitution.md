<!--
Sync Impact Report
==================
Version change: (template, unversioned) → 1.0.0
Bump rationale: initial ratification — all placeholders replaced with concrete principles.

Principles defined (template slot → new title):
  - [PRINCIPLE_1_NAME] → I. Free, Ad-Free, Private (NON-NEGOTIABLE)
  - [PRINCIPLE_2_NAME] → II. Correctness of Calculations (NON-NEGOTIABLE)
  - [PRINCIPLE_3_NAME] → III. Simplicity
  - [PRINCIPLE_4_NAME] → IV. Testability
  - [PRINCIPLE_5_NAME] → V. Localization
  - (added)            → VI. Version Control
  - (added)            → VII. Distribution

Sections:
  - [SECTION_2_NAME] → Technical Constraints
  - [SECTION_3_NAME] → Development Workflow & Quality Gates
  - Governance       → filled

Removed sections: none

Templates:
  ✅ .specify/templates/plan-template.md   — Constitution Check gates enumerated per principle
  ✅ .specify/templates/spec-template.md   — added "Constitution Constraints" section
                                             (sources, units/precision, dependencies, permissions,
                                             localization)
  ✅ .specify/templates/tasks-template.md  — tests mandatory for calculation logic (test-first);
                                             localization + release-readiness tasks in Polish phase
  ✅ .specify/templates/checklist-template.md — no principle references; no change needed
  ✅ .specify/templates/commands/*.md      — directory not present; nothing to check
  ⚠ README.md                              — not present; create when the project is published

Follow-up TODOs:
  - Project directory and Gradle rootProject.name are still "circuitopedia"; the product name in
    this constitution is "Voltpedia". Align naming (settings.gradle.kts, applicationId, app_name,
    release artifact names) in a dedicated change.
-->

# Voltpedia Constitution

Voltpedia is a free, ad-free, open-source electronics and electrical engineering reference and
calculator app for Android, written in Kotlin. It provides calculators (Ohm's law, resistor
color/SMD codes, voltage dividers, LED resistors, RC/LC, 555 timer, wire gauge, voltage drop,
power, unit conversion, etc.), reference tables, pinouts and formulas as an alternative to apps
such as ElectroDoc and ElectroCalc.

## Core Principles

### I. Free, Ad-Free, Private (NON-NEGOTIABLE)

- The app MUST NOT contain ads, in-app purchases, paywalled features or subscriptions.
- The app MUST NOT include analytics, tracking or crash-reporting SDKs, and MUST NOT make any
  network call that sends user data.
- The `INTERNET` permission MUST NOT be declared unless a feature strictly requires it and its
  spec justifies the need; no such feature may transmit user data.
- Every feature MUST work fully offline.

**Rationale**: Users rely on the app as a trustworthy bench tool; privacy and offline use are the
product's core differentiators from ad-supported alternatives.

### II. Correctness of Calculations (NON-NEGOTIABLE)

- Every formula and reference value MUST cite its source (standard, datasheet or textbook) in a
  code comment next to the implementation or in the feature spec.
- All calculations MUST operate on explicit units with SI prefixes; unit-less raw numbers MUST NOT
  cross module boundaries.
- All inputs MUST be validated. Invalid or physically impossible input (e.g. negative resistance,
  division by zero, out-of-range values) MUST produce a clear, localized message — never a crash,
  `NaN`/`Infinity` on screen, or a silently wrong result.
- Each calculator's spec MUST define its rounding and precision rules (significant digits,
  rounding mode, E-series snapping where relevant), and the implementation MUST apply them
  consistently.

**Rationale**: A wrong value in an engineering reference can damage hardware or endanger people;
traceable sources and explicit units make every result verifiable.

### III. Simplicity

- Every new library MUST be justified in the spec: what it replaces and why the Kotlin standard
  library or platform API is insufficient. Kotlin stdlib and official AndroidX/Jetpack libraries
  are preferred.
- No speculative abstractions: interfaces, extra layers, DI frameworks and generic engines MUST NOT
  be introduced until at least two concrete uses exist (YAGNI).
- All calculation logic, data models, unit conversion and reference data MUST live in pure Kotlin
  with no `android.*` imports. Android APIs are confined to the UI and app-shell layer.
- Functions MUST be small and readable; clarity takes precedence over cleverness.

**Rationale**: A small, dependency-light codebase is easier to audit, keeps the APK small and lets
the core logic be tested on the plain JVM.

### IV. Testability

- Pure calculation and conversion logic MUST be covered by fast JVM unit tests, including edge
  cases (zero, negative, very large and very small values, unit/prefix boundaries) and known
  reference values taken from the cited sources.
- Calculation logic MUST be developed test-first: failing tests are written from the spec's
  expected values before the implementation.
- UI behavior that carries logic (input parsing, error display, locale formatting) MUST be tested;
  purely visual layout does not require tests.
- All tests MUST pass before any merge into `main` or any release.

**Rationale**: Tests derived from cited reference values are the executable proof of Principle II.

### V. Localization

- The development language (code, comments, commits, specs, docs) is English.
- Supported app languages: English (`en`, base and fallback), German (`de`), French (`fr`),
  Spanish (`es`), Japanese (`ja`), Ukrainian (`uk`).
- User-facing strings MUST NOT be hardcoded; all text goes through Android string resources,
  using plurals resources wherever a count is shown.
- Number, decimal-separator and unit formatting MUST respect the user's locale. Input parsing MUST
  accept both `.` and `,` as the decimal separator.
- A feature is not complete until all six locales contain its translations. Missing keys MUST fall
  back to English and MUST NOT cause a crash or display raw resource keys.

**Rationale**: The app serves an international audience of engineers and hobbyists; correct locale
handling is part of calculation correctness (a misparsed separator is a wrong result).

### VI. Version Control

- Git is used for all work. Each spec is developed on its own branch named after the spec using
  Spec Kit numbering (e.g. `001-ohms-law-calculator`).
- A branch MAY merge into `main` only when all of the spec's tasks are complete and all tests pass.
- Commits MUST be small, focused and written in English.

**Rationale**: One branch per spec keeps history traceable from requirement to code.

### VII. Distribution

- The app is distributed as signed APKs via GitHub Releases. Google Play-specific dependencies
  (Play Services, Play Billing, Play Core) MUST NOT be used.
- App versions follow Semantic Versioning; every release MUST ship with an English changelog.
- Release builds MUST be reproducible from a tagged commit. Signing keys and keystore passwords
  MUST NOT be committed to the repository.

**Rationale**: Independence from proprietary store services keeps the app free, private and
installable on de-Googled devices.

## Technical Constraints

- Language: Kotlin. Target platform: Android.
- Architecture: a pure-Kotlin core (calculations, models, units, reference data) with no Android
  dependencies, plus a thin Android UI/app-shell layer.
- Permissions: none by default; any permission MUST be justified in the spec of the feature that
  introduces it.
- Dependencies: Kotlin stdlib and AndroidX/Jetpack preferred; any other library requires a written
  justification in the spec (Principle III).
- Reference data (tables, pinouts, color codes) is bundled with the app and carries its source
  citation.

## Development Workflow & Quality Gates

1. `/speckit-specify` creates the spec on a numbered branch; the spec MUST list formula/reference
   sources, units, precision rules, any new dependency and any new permission.
2. `/speckit-plan` MUST pass the Constitution Check; any violation is recorded with justification
   in the plan's Complexity Tracking table or the design is changed.
3. `/speckit-tasks` MUST include test-first tasks for calculation logic and translation tasks for
   all six locales.
4. Before merge: all tasks done, all tests green, no hardcoded strings, no new network calls or
   tracking code, all locales translated.
5. Before release: version bumped per SemVer, English changelog written, signed APK built from a
   tagged commit.

## Governance

- This constitution supersedes all other practices. Every spec, plan and task list MUST be checked
  against it; violations MUST be explicitly justified in the plan's Complexity Tracking section or
  rejected. Principles I and II are NON-NEGOTIABLE and cannot be waived by justification.
- Amendments require a documented rationale, an update to this file (including the Sync Impact
  Report) and a version bump of the constitution:
  - MAJOR: removing or redefining a principle;
  - MINOR: adding a principle or section, or materially expanding guidance;
  - PATCH: clarifications and wording.
- After each amendment, the dependent templates (`.specify/templates/plan-template.md`,
  `spec-template.md`, `tasks-template.md`) MUST be updated to stay consistent.
- Compliance is reviewed at each Constitution Check during planning and again before every merge
  into `main`.

**Version**: 1.0.0 | **Ratified**: 2026-10-31 | **Last Amended**: 2026-10-31
