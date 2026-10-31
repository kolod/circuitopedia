# Data Model: App Shell with Category Navigation

**Feature**: [spec.md](spec.md) | **Date**: 2026-10-31

The feature stores nothing on disk. All entities are compile-time values or saved UI state.

## Category

The top-level grouping shown as a tab.

| Field   | Type        | Notes                                                    |
|---------|-------------|----------------------------------------------------------|
| entries | enum values | `Calculations`, `Reference`, declared in this exact order |

- The order of the enum entries is the tab order (FR-002), and the first entry is the default
  tab (FR-003).
- The shell maps each value to a localized tab label and a localized empty-state message
  (`app` resources). The enum itself holds no Android types.
- The set is fixed. Modules cannot add categories.

## ModuleId

The stable identifier of a module.

| Field | Type   | Validation                                                         |
|-------|--------|--------------------------------------------------------------------|
| value | String | Must match `[a-z0-9]+(-[a-z0-9]+)*`, length 1–64; rejected at construction |

- Never shown to users. Used for navigation and saved state (FR-015).
- Must be unique across the registry (FR-014, checked by a unit test).
- Must never change once a module has shipped, because saved state of users who update the app
  refers to it.

## AppModule (Resource)

The descriptor that each calculator or reference page module provides.

| Field          | Type                     | Validation / notes                                    |
|----------------|--------------------------|-------------------------------------------------------|
| id             | ModuleId                 | see above                                             |
| category       | Category                 | exactly one                                           |
| titleRes       | string resource id       | must exist in default (`en`) resources and in all five other locales (Lint) |
| descriptionRes | string resource id       | one line, same rules as `titleRes`                    |
| iconRes        | drawable resource id     | vector drawable bundled with the module               |
| content        | composable, no arguments | the module's screen body. The shell draws the top bar and "up" control around it |

- Belongs to exactly one Category.
- Has no references to other modules or to the shell.

## ModuleRegistry

The single explicit list of modules included in the app.

| Field   | Type              | Notes                                          |
|---------|-------------------|------------------------------------------------|
| modules | List\<AppModule\> | Lives in `app`. Empty in this release (FR-016)  |

- Its order is not shown to users, because lists are sorted by localized title (FR-008). The
  order only keeps diffs readable (one line per module).

## Navigation state (saved UI state, not persisted)

| State           | Type           | Default              | Survives                                   |
|-----------------|----------------|----------------------|--------------------------------------------|
| selected page   | Int (pager)    | 0 (`Calculations`)   | rotation, config change, process recreation |
| scroll position | per Category   | top                  | same, plus tab switches and module round trips |
| openModuleId    | String?        | `null` (catalog)     | same                                       |

State transitions:

```text
            tap entry (id)                    
Catalog ───────────────────────► ModuleScreen(id)
   ▲                                  │
   └──── system back / "up" ──────────┘

Catalog ── system back ──► app exits (default Android behavior)

Restore with openModuleId not in registry ──► Catalog
Cold start (user closed app) ──► Catalog, page 0
```
