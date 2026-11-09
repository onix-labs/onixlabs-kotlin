![ONIX Labs](https://raw.githubusercontent.com/onix-labs/onixlabs-website/refs/heads/main/OnixLabs.Web/wwwroot/onixlabs/images/logo/logo-full-light.svg)

# ONIXLabs Kotlin Library

[![Build](https://github.com/onix-labs/onixlabs-kotlin/actions/workflows/build.yml/badge.svg)](https://github.com/onix-labs/onixlabs-kotlin/actions/workflows/build.yml)

The ONIXLabs Kotlin Library is a suite of general-purpose libraries for the JVM, designed to support modern application development with a strong emphasis on correctness, type safety, and expressive, idiomatic Kotlin. They are designed to integrate cleanly into both small utilities and large-scale systems, without imposing architectural constraints or framework lock-in.

At a high level, ONIXLabs provides:

-   Foundational extensions and patterns missing or underrepresented in the Kotlin standard library
-   A declarative object graph DSL that describes a type once and reuses that description everywhere
-   Fluent, inspectable validation of object graphs
-   Convention-based object-to-object projection that respects the Kotlin type system
-   Calendar-aware scheduling of recurring tasks

Current JVM support includes Java 17 and later, and all packages are published to [GitHub Packages](https://github.com/orgs/onix-labs/packages?repo_name=onixlabs-kotlin).

## Package Overview

-   **onixlabs-kotlin-core** – Foundational extensions, specifications, and type conversion
-   **onixlabs-kotlin-graph** – Declarative object graph DSL and type registry
-   **onixlabs-kotlin-graph-validation** – Object graph validation, as a graph plugin
-   **onixlabs-kotlin-graph-projection** – Object-to-object projection, as a graph plugin
-   **onixlabs-kotlin-scheduler** – Calendar-aware scheduling of recurring tasks

## onixlabs-kotlin-core

[See it on GitHub Packages](https://github.com/orgs/onix-labs/packages?repo_name=onixlabs-kotlin)

Foundational extensions and patterns that address common gaps in the Kotlin standard library. This package provides low-level building blocks used throughout the ONIXLabs ecosystem, but is also useful as a standalone utility library in any Kotlin application.

-   Specification pattern with logical composition (`Specification<T>`, `and`, `or`, `not`)
-   Built-in specifications for strings, iterables, comparables, booleans, and generic values
-   Type-safe type converters with overflow and precision checks (`TypeConverter<T>`)
-   Ordinal day-of-month types for calendar logic (`DayOfMonth`, `DayOfMonthOccurrence`)
-   Extensions for numeric types, `BigDecimal`, strings, comparables, and iterables
-   Reflection helpers for `KClass`, `Class`, and `Type`
-   String case formatting (snake case and kebab case)
-   Terminal colour output (`TerminalColor`)

## onixlabs-kotlin-graph

[See it on GitHub Packages](https://github.com/orgs/onix-labs/packages?repo_name=onixlabs-kotlin)

A declarative DSL for describing object graphs. A graph is built once, is immutable, and can be shared across threads; plugins such as validation and projection interpret it, and a registry resolves nested types automatically.

-   Graph DSL root (`graph<T> { ... }`) and graph registry (`graphs { ... }`)
-   Member model for properties, collection elements, and map entries
-   Graph paths for precise member addressing (`Customer.orders[2].total`)
-   Extension points for graph plugins

## onixlabs-kotlin-graph-validation

[See it on GitHub Packages](https://github.com/orgs/onix-labs/packages?repo_name=onixlabs-kotlin)

Fluent, declarative validation of object graphs. Validation rules are built on `Specification<T>`, are reusable across types, and can be inspected without a subject.

-   Validation DSL (`validate { ... }`) for properties, collections, and maps
-   Built-in rules for strings, iterables, maps, comparables, booleans, and generic values
-   Nested validation of members using their registered graphs
-   Hierarchical validation results with graph paths and machine-readable condition identifiers
-   Stop-on-error validation
-   Rule descriptions without a subject (`describe<T>()`)

## onixlabs-kotlin-graph-projection

[See it on GitHub Packages](https://github.com/orgs/onix-labs/packages?repo_name=onixlabs-kotlin)

Unobtrusive, convention-based object-to-object projection that respects the Kotlin type system and reduces the need for manual mapping code. In the best case, a projection requires no configuration at all.

-   Projection DSL (`project(R::class) { ... }`) for unconventional mappings
-   Convention-based projection by constructor parameter and property name
-   Configurable type conversion (`typeConverter<T>(...)`) and target factories (`projectionTarget<T> { ... }`)
-   Nested and collection projection using registered graphs
-   Descriptive projection failures, all reported at once with paths (`ProjectionException`)

## onixlabs-kotlin-scheduler

[See it on GitHub Packages](https://github.com/orgs/onix-labs/packages?repo_name=onixlabs-kotlin)

Calendar-aware scheduling of recurring tasks, supporting rules such as "the second Tuesday of every month at 09:00", "every other Tuesday at 09:00", or "the last Friday of March, June, September, and December".

-   Interval, daily, weekly, monthly, and yearly schedules
-   Ordinal day-of-month rules (`first`, `second`, ..., `last`)
-   Explicit time zones for every schedule
-   Next-occurrence calculation for any schedule
-   Task scheduler with per-task failure isolation, overlap and misfire policies, and outcome listeners
