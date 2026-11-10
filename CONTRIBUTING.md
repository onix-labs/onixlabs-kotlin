# Contributing

Thanks for your interest in contributing to ONIXLabs Kotlin! This guide explains how to build the project and propose changes.
By participating, you agree to abide by the [Code of Conduct](CODE_OF_CONDUCT.md).

## Reporting bugs and requesting features

Please [open an issue](https://github.com/onix-labs/onixlabs-kotlin/issues/new/choose) using one of the templates. For security
vulnerabilities, do **not** open a public issue; follow the [security policy](SECURITY.md) instead.

## Building

You need [JDK 25](https://www.azul.com/downloads/?package=jdk#zulu) to build; the libraries are compiled to JVM 17 bytecode and
tested on JDK 17, 21, and 25 in CI. Gradle is provided by the wrapper.

```shell
./gradlew build                 # compile, test, check formatting and the public API, and measure coverage
./gradlew test -PtestJdk=17     # run the tests on another installed JDK
./gradlew :playground:run       # run the playground
```

The build is organised as one module per library (`core/`, `graph/`, `graph-validation/`, `graph-projection/`, `scheduler/`),
with shared configuration in the convention plugins under `build-logic/`, and dependency versions in `gradle/libs.versions.toml`.

## Making changes

1. Fork the repository, and create a branch from `main`.
2. Make your change, with tests. Every public declaration needs an explicit visibility and KDoc (the build uses explicit API mode).
3. Run `./gradlew spotlessApply` to apply the licence header to new files.
4. If you changed the public API, run `./gradlew updateKotlinAbi` and commit the updated `<module>/api/*.api` files, so that the
   change is visible in review. `./gradlew build` fails if the public API changes without them.
5. Run `./gradlew build`, and make sure it passes.
6. Open a pull request against `main`. Describe what changed and why; link any related issue.

`main` is protected: changes reach it only through pull requests that pass CI on JDK 17, 21, and 25, with review conversations resolved.

## Commit messages

Write a short summary line in the imperative mood ("Add map entry rules", not "Added map entry rules"), followed by a blank line and a
body that explains what changed and why.

## Licence

By contributing, you agree that your contributions will be licensed under the [Apache License, Version 2.0](LICENSE).
