# Changelog

All notable changes are documented here. Versions follow [Semantic Versioning](https://semver.org/).

## [Unreleased]

## [1.0.0] - 2026-07-28

### Added

- event-driven town, population, warehouse, construction, and duplication workflows;
- explicit construction, single-track, upgrading, and dual-track railway states;
- alternating single-track and bidirectional dual-track freight capacity;
- validated entity factories and a referentially safe network registry;
- observer-driven console and Graphviz reporting;
- deterministic environmental boundaries for clocks, input, output, paths, ticks, and event sources;
- focused tests across event generation, entities, lifecycle policy, registry behavior, reporting, and full-day orchestration;
- Java 21 Gradle build, compiler linting, PMD, CI, and dependency automation;
- architecture, security, contribution, event-language, and usage documentation.

### Changed

- separated composition, input, domain, network, observer, presentation, and simulation concerns;
- replaced string-based railway phases with a typed status and polymorphic State model;
- centralized validation and network mutation behind `NetworkManager`;
- replaced static file logging with focused console reporting and generated DOT output;
- preserved active single-track service while a route is being duplicated.

### Removed

- generated distributions, cached build output, local logs, IDE metadata, and reference documents.

[Unreleased]: https://github.com/Himath2002/railway-network-simulator/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/Himath2002/railway-network-simulator/releases/tag/v1.0.0
