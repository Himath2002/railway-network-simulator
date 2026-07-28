# Architecture notes

Railway Network Simulator separates environmental input, infrastructure policy, orchestration, and presentation so the model can be tested without a live clock, console, or filesystem.

## Dependency direction

```text
application ─┬─> input
             ├─> network ───────> domain <──── factory
             ├─> presentation ──> domain
             └─> simulation ────> input + network + presentation + domain

network ─────> observer <──────── presentation
domain/state ─> domain/entity
```

The domain never imports application, input, presentation, simulation, logging, or Gradle concerns.

## Day transaction

`Simulation` owns the order of work for one day:

1. drain the current event batch;
2. remove exact duplicates while preserving arrival order;
3. validate and apply each event independently;
4. produce goods in every town;
5. ask every railway state for directional capacity;
6. remove available freight from its origin;
7. advance infrastructure states once;
8. remove non-persistent observers;
9. print a daily snapshot;
10. write Graphviz output only when an observed topology mutation occurred.

Invalid events are reported and skipped. They are not included in the accepted-event count and do not prevent valid events in the same batch from executing.

## Domain invariants

- town and warehouse names are non-blank;
- town populations are non-negative after creation;
- warehouse capacities are positive and stock stays within `[0, capacity]`;
- a railway always connects two distinct, non-null towns;
- registered routes are unique regardless of endpoint order;
- warehouses and railways reference registered towns;
- construction and upgrade durations are positive;
- stock, production, capacity, and transport values never become negative;
- collections crossing registry boundaries are snapshots rather than mutable internals.

These invariants prevent partially valid infrastructure from entering the simulation loop.

## State boundary

`RailwayState` owns five related decisions:

- whether an origin may send freight on a given day;
- the route capacity for an origin-destination pair;
- what happens after a completed day;
- whether duplication may begin;
- how the phase is represented in Graphviz.

This keeps phase-specific logic together and makes invalid endpoint checks consistent. The transition chain is:

```text
UnderConstructionState
    -- 5 completed days -->
SingleTrackState
    -- accepted duplication request -->
SingleTrackUpgradingState
    -- 5 completed days -->
DualTrackState
```

The upgrading state delegates capacity to an active `SingleTrackState`, preserving service during construction.

## Network boundary

`NetworkManager` is the application-facing facade. It:

- validates names, quantities, town existence, and route eligibility;
- delegates typed construction to factories;
- preserves referential integrity through `EntityRegistry`;
- prevents directional duplicates such as `A--B` and `B--A`;
- exposes defensive entity snapshots;
- notifies observers after relevant network mutations;
- coordinates freight withdrawal across a town and its warehouses.

The registry owns identity. The simulation does not scan or mutate backing collections directly.

## Environmental boundaries

The default application uses real implementations, while tests inject deterministic substitutes:

| Concern | Production | Test boundary |
| --- | --- | --- |
| Events | `RandomNetworkEventSource` | queue-backed `NetworkEventSource` |
| Generator time | wall clock | injected `LongSupplier` |
| Stop signal | `System.in` | `ByteArrayInputStream` |
| Day duration | one second | `Duration.ZERO` |
| Console | `System.out` | `ByteArrayOutputStream` |
| Graph path | `build/outputs/...` | temporary directory |

The result is an end-to-end simulation test that executes the real orchestration without sleeping or polluting the repository.

## Presentation boundary

`NetworkReporter` is both a presenter and a persistent topology observer. Console summaries are deterministic, and Graphviz output:

- quotes node identifiers;
- escapes backslashes and quotation marks;
- uses UTF-8;
- creates the configured parent directory;
- distinguishes building, single, upgrading, and dual states;
- writes only after an observed change.

File errors are reported without crashing the running model.

## Extension checklist

When adding behavior:

1. preserve domain and registry invariants;
2. translate external formats behind `NetworkEventSource`;
3. keep lifecycle behavior inside `RailwayState`;
4. keep cross-entity validation inside `NetworkManager`;
5. keep time and event order inside `Simulation`;
6. keep output formatting inside `NetworkReporter`;
7. test both the focused boundary and a representative day;
8. update the README and changelog when grammar or semantics change.
