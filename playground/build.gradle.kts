plugins {
    id("onixlabs.kotlin-jvm")
    application
}

application {
    mainClass.set("io.onixlabs.kotlin.playground.ProgramKt")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":graph"))
    implementation(project(":graph-projection"))
    implementation(project(":graph-validation"))
    implementation(project(":scheduler"))
}
