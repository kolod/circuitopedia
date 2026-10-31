# Feature Specification: App Shell with Category Navigation

**Feature Branch**: `001-app-shell-navigation`

**Created**: 2026-10-31

**Status**: Draft

**Input**: User description: "Build an app that can be built and launched, with navigation by category: Calculations and Reference, shown as tabs at the top of the window. The main part of the window is the list of resources available in the selected category. No actual calculators or reference pages yet. Also design how individual modules will be integrated into the app."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Launch the app and switch between categories (Priority: P1)

A user installs the app and opens it. At the top of the screen they see two tabs, "Calculations"
and "Reference". The Calculations tab is selected on first launch. The rest of the screen shows
what is available in the selected category. Tapping the other tab (or swiping horizontally across
the content) switches the category and its content.

**Why this priority**: This is the first runnable version of the product. Every later
calculator and reference page lives in this structure, so it has to exist before anything else
can ship.

**Independent Test**: Install the build on a device or emulator, launch it, and switch between
the two tabs. The user can see which tab is selected and the content changes to match.

**Acceptance Scenarios**:

1. **Given** the app is freshly installed, **When** the user launches it, **Then** the main
   screen opens with the "Calculations" tab selected and the two tabs visible at the top.
2. **Given** the "Calculations" tab is selected, **When** the user taps "Reference", **Then**
   "Reference" is shown as selected and the content area shows the Reference category.
3. **Given** a tab is selected, **When** the user swipes horizontally across the content area,
   **Then** the adjacent category becomes selected, and the tab indicator follows.
4. **Given** a tab is selected, **When** the device is rotated or the system recreates the
   screen, **Then** the same tab stays selected and the list scroll position is kept.

---

### User Story 2 - Browse the list of resources in a category (Priority: P1)

In the selected category the user sees a scrollable list of the resources (calculators or
reference pages) it contains. Each entry shows an icon, a title and a one-line description in
the user's language. When a category has no resources yet, the user sees a clear message saying
that nothing is available there yet, instead of a blank screen.

**Why this priority**: The list is the main surface of the app. Without it the tabs have no
purpose. In this release no real calculators or reference pages exist, so the empty state is
what users will actually see.

**Independent Test**: Launch the build with no resources and check that each tab shows the
localized empty-state message. Launch a test build with sample resources and check that each
entry appears in the right category with its icon, title and description, in the expected order.

**Acceptance Scenarios**:

1. **Given** a category has no resources, **When** the user opens its tab, **Then** a localized
   message explains that no items are available in this category yet.
2. **Given** a category has several resources, **When** the user opens its tab, **Then** every
   resource of that category is listed exactly once, and no resource of the other category
   appears.
3. **Given** a category has more resources than fit on screen, **When** the user scrolls,
   **Then** the list scrolls smoothly and all entries can be reached.
4. **Given** the device language is one of the supported locales, **When** the user opens a
   tab, **Then** tab names, titles, descriptions and the empty-state message are in that
   language. Entries are sorted alphabetically by their localized title using that language's
   collation rules.

---

### User Story 3 - Open a resource and return to the list (Priority: P2)

The user taps an entry in the list and the resource's own screen opens. The system back action
(gesture or button) or the screen's "up" control returns the user to the same tab, with the list
scrolled to where they left it.

**Why this priority**: This is the path every future calculator and reference page will use.
It is P2 only because no real resources exist in this release, so it is checked with sample
resources in test builds.

**Independent Test**: In a test build with sample resources, tap an entry, check that its
screen opens with its title, go back, and check that the original tab and scroll position are
restored.

**Acceptance Scenarios**:

1. **Given** the list shows a resource, **When** the user taps it, **Then** that resource's
   screen opens with its title in the top bar and an "up" control.
2. **Given** a resource screen is open, **When** the user presses system back or "up", **Then**
   the main screen returns with the same tab selected and the same scroll position.
3. **Given** the main screen is shown, **When** the user presses system back, **Then** the app
   exits normally, as Android apps do.

---

### User Story 4 - Add a new module without changing the app shell (Priority: P2)

A contributor wants to add a new calculator or reference page. They create a self-contained
module that describes itself (stable identifier, category, localized title, short description,
icon) and provides its own screen, then add that module to the app's single list of modules.
The new module then shows up in the right category's list and opens when tapped. Nothing in the
tabs, the list or the navigation needs to change.

**Why this priority**: The user explicitly asked to design module integration now. The
contract set here decides how cheaply every future calculator and reference page can be added.
It is P2 because end users never see it directly.

**Independent Test**: Add a sample module in a test configuration, run the app or its tests,
and check that the module appears in the declared category and opens. Add two modules with the
same identifier and check that the problem is caught by an automated check before the app ships.

**Acceptance Scenarios**:

1. **Given** a new module declaring category "Calculations", **When** it is added to the module
   list, **Then** it appears in the Calculations tab and not in the Reference tab, with no other
   change to the app shell.
2. **Given** two modules declare the same identifier, **When** the project's automated checks
   run, **Then** they fail with a message naming the duplicated identifier.
3. **Given** a module is missing a translation for one of the supported locales, **When** the
   app runs in that locale, **Then** the module's English text is shown instead and the app does
   not crash or show a raw resource key.
4. **Given** a module is removed from the module list, **When** the app is built and launched,
   **Then** the module no longer appears anywhere and nothing else breaks.

---

### Edge Cases

- A category has zero resources: the localized empty-state message is shown (this is the normal
  state in this release).
- Very long titles or descriptions (for example German): the title wraps to at most two lines
  and the description is cut to one line with an ellipsis. The layout never overlaps or clips
  the icon.
- Large system font size (up to 200%): tabs, list entries and the empty-state message stay
  readable and usable. Tab labels may wrap or shrink, but must not be cut so short that they
  can't be recognized.
- Tapping an entry twice quickly opens the resource screen only once.
- Unsupported device language: the app falls back to English.
- Dark theme enabled in the system: the app follows the system light or dark theme.
- Tablet-sized screens: tabs stay at the top and the list stays usable. A dedicated two-pane
  tablet layout is out of scope. The app is portrait-only (constitution 1.1.0).
- The process is killed in the background and restored: the app reopens on the tab and screen
  the user last had open.

## Requirements *(mandatory)*

### Functional Requirements

**App shell & navigation**

- **FR-001**: The project MUST produce an installable Android app that launches to the main
  screen without errors on a supported device or emulator.
- **FR-002**: The main screen MUST show exactly two category tabs at the top, in this order:
  "Calculations" and "Reference".
- **FR-003**: On a fresh launch, the "Calculations" tab MUST be selected.
- **FR-004**: Users MUST be able to switch categories by tapping a tab and by swiping
  horizontally across the content area. The selected tab MUST always be visually marked.
- **FR-005**: The selected tab, the open screen and each list's scroll position MUST survive
  device rotation, configuration changes and process recreation.

**Resource list**

- **FR-006**: The content area MUST show a vertically scrollable list of all resources whose
  declared category matches the selected tab.
- **FR-007**: Each list entry MUST show the resource's icon, localized title and localized
  one-line description. The whole entry MUST be a single tap target.
- **FR-008**: Entries MUST be sorted alphabetically by localized title, using the collation
  rules of the current app language.
- **FR-009**: When a category has no resources, the content area MUST show a localized
  empty-state message instead of an empty list.
- **FR-010**: Tapping an entry MUST open that resource's screen. System back and the "up"
  control on that screen MUST return to the main screen with the previous tab and scroll
  position.

**Module integration**

- **FR-011**: Each calculator or reference page MUST be a self-contained module that declares:
  a stable unique identifier, exactly one category (Calculations or Reference), a localized
  title, a localized one-line description, an icon, and its own screen.
- **FR-012**: The app MUST get the set of available modules from one explicit, ordered list of
  modules. Adding or removing a module MUST change only that list and the module itself, not
  the tabs, the list screen or the navigation.
- **FR-013**: A module MUST NOT depend on another module. A module's calculation logic and
  reference data MUST stay usable and testable without the app shell.
- **FR-014**: The project's automated checks MUST fail when two modules share an identifier,
  or when a module is missing its title or description in English (the fallback language).
- **FR-015**: Each module's screen MUST be reachable by its stable identifier, so that
  navigation and state restoration don't depend on the module's position in any list.
- **FR-016**: In this release, the shipped module list MUST be empty. Sample modules used to
  check list rendering, ordering, opening and the integration contract MUST exist only in
  tests and MUST NOT ship in the release app.

**Product baseline**

- **FR-017**: The app's display name on the launcher and in the system MUST be "Circuitopedia".
- **FR-018**: The app MUST follow the system light or dark theme. It runs in portrait orientation
  only (amended 2026-10-31 per constitution 1.1.0; originally portrait and landscape).

### Key Entities

- **Category**: One of the two top-level groupings, Calculations or Reference. It has a fixed
  order and a localized name. The set of categories is fixed in this release.
- **Module (Resource)**: A self-contained calculator or reference page. Its attributes are a
  stable identifier, its category, a localized title, a localized short description, an icon,
  and an entry screen. It belongs to exactly one category.
- **Module list (Registry)**: The single explicit list of all modules included in the app. It
  is the only place that decides which modules exist.

## Constitution Constraints *(mandatory)*

### Sources *(Principle II)*

- None. This feature contains no formulas or reference values.

### Units, Validation & Precision *(Principle II)*

- **Inputs and units**: None. The shell takes no numeric input.
- **Invalid input**: None. Malformed module declarations (duplicate identifier, missing English
  title or description) are caught by automated checks (FR-014), not at runtime.
- **Precision/rounding**: None.

### New Dependencies *(Principle III)*

- None beyond the official AndroidX/Jetpack libraries already in the project's version catalog.
  If in-app screen navigation needs an additional official Jetpack navigation library, the plan
  MUST justify it there.

### Permissions & Network *(Principle I)*

- None. No permissions and no network access. The app works fully offline.

### Localization *(Principle V)*

- New user-facing strings (app name, the two tab names, the empty-state message, accessibility
  labels for tabs and the "up" control) require translations in en, de, fr, es, ja and uk.
- The app name "Circuitopedia" is a proper name and is not translated, but it is still provided
  for every locale.
- Sorting by title (FR-008) uses the collation of the current app language, which matters for
  Ukrainian, German, Spanish, French and Japanese.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A contributor with a fresh checkout can build the app and launch it on an emulator
  using only the documented project build command, with no manual setup beyond the Android SDK.
- **SC-002**: From a cold start, the main screen with tabs is visible in under 1 second on a
  mid-range device.
- **SC-003**: Switching between the two tabs takes the user one action (one tap or one swipe),
  and the new category's content is visible immediately with no loading indicator.
- **SC-004**: Adding a new sample module touches only the new module and one line in the module
  list. No file of the app shell (tabs, list, navigation) changes.
- **SC-005**: All shell UI text appears in each of the six supported languages, with no missing
  or raw-key strings, checked on every locale.
- **SC-006**: In 10 consecutive cycles of rotation and background/restore, the selected tab and
  scroll position are kept every time.

## Assumptions

- The product name is "Circuitopedia", as set in the constitution (v1.0.1) and matching the
  repository and the Gradle project name.
- The release app ships with no modules, so both tabs show the empty-state message. Real
  calculators and reference pages come in later specs, one module each.
- Both categories use a single flat list. Subcategories, search, favorites and recently used
  items are out of scope and may be added in later specs.
- The category set (Calculations, Reference) is fixed and is not provided by modules.
- On a cold start (after the user fully closes the app), the app opens on the Calculations tab.
  Only recreation by the system restores the previous tab and screen.
- The minimum supported Android version and the exact UI toolkit are decided in the plan, within
  what the existing project configuration already sets up.
- A module's icon comes from icons bundled with the app. No icon is downloaded.
