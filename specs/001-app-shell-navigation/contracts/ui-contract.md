# Contract: Shell UI

**Feature**: [../spec.md](../spec.md)

This file describes what users see and which strings the shell owns. Translations are written
during implementation.

## Main (catalog) screen

```text
┌──────────────────────────────────┐
│ Circuitopedia                    │  top app bar: app name
├────────────────┬─────────────────┤
│  Calculations  │    Reference    │  PrimaryTabRow, indicator under selected tab
├────────────────┴─────────────────┤
│ [icon] Title (≤ 2 lines)         │  ListItem, whole row clickable
│        Description (1 line, …)   │
│ [icon] Title                     │
│        Description               │
│              …                   │  HorizontalPager page = one Category
└──────────────────────────────────┘
```

Empty category (the only state in this release):

```text
├──────────────────────────────────┤
│                                  │
│     No calculators yet.          │  centered, body style
│                                  │
└──────────────────────────────────┘
```

## Module screen

```text
┌──────────────────────────────────┐
│ ←  Module title                  │  TopAppBar, "up" = auto-mirrored arrow
├──────────────────────────────────┤
│        module.content()          │
└──────────────────────────────────┘
```

## Shell strings (`app/src/main/res/values*/strings.xml`)

| Key                       | English (base)              | Notes                                   |
|---------------------------|-----------------------------|-----------------------------------------|
| `app_name`                | Circuitopedia               | proper name, same in all locales        |
| `tab_calculations`        | Calculations                |                                         |
| `tab_reference`           | Reference                   |                                         |
| `empty_calculations`      | No calculators yet.         | shown on an empty Calculations tab      |
| `empty_reference`         | No reference pages yet.     | shown on an empty Reference tab         |
| `navigate_up`             | Navigate up                 | content description of the "up" button  |

Every key must exist in `values`, `values-de`, `values-fr`, `values-es`, `values-ja` and
`values-uk` (Lint `MissingTranslation` = error).

## Behavior rules

- Title: `maxLines = 2`, ellipsis. Description: `maxLines = 1`, ellipsis.
- The theme follows the system light/dark setting. Dynamic color is used on Android 12+.
- Layout uses edge-to-edge with system bar insets applied by `Scaffold`.
- Back on the catalog finishes the activity. Back on a module screen returns to the catalog.
