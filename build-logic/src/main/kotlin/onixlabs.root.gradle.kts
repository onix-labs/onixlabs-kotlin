// The root project: aggregates documentation and coverage across every library.

plugins {
    id("org.jetbrains.dokka")
    id("org.jetbrains.kotlinx.kover")
}

dokka {
    moduleName.set("ONIXLabs Kotlin Library")
}
