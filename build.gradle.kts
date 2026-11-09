plugins {
    id("onixlabs.root")
}

dependencies {
    dokka(project(":core"))
    dokka(project(":graph"))
    dokka(project(":graph-projection"))
    dokka(project(":graph-validation"))
    dokka(project(":scheduler"))

    kover(project(":core"))
    kover(project(":graph"))
    kover(project(":graph-projection"))
    kover(project(":graph-validation"))
    kover(project(":scheduler"))
}
