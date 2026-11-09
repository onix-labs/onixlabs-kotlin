import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

// A published ONIXLabs library: explicit API, ABI checks, documentation, coverage, and publishing.
// Each library sets its own `description`, which becomes the POM description.

plugins {
    id("onixlabs.kotlin-jvm")
    id("org.jetbrains.dokka")
    id("org.jetbrains.kotlinx.kover")
    `maven-publish`
}

kotlin {
    explicitApi()

    @OptIn(ExperimentalAbiValidation::class)
    abiValidation()
}

java {
    withSourcesJar()
}

val javadocJar = tasks.register<Jar>("javadocJar") {
    description = "Assembles a Javadoc JAR from the Dokka HTML documentation."
    archiveClassifier.set("javadoc")
    from(tasks.named("dokkaGeneratePublicationHtml"))
}

publishing {
    publications {
        register<MavenPublication>("library") {
            artifactId = "onixlabs-kotlin-${project.name}"
            from(components["java"])
            artifact(javadocJar)

            pom {
                name.set("ONIXLabs Kotlin ${project.name}")
                description.set(provider { project.description })
                url.set("https://github.com/onix-labs/onixlabs-kotlin")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("onix-labs")
                        name.set("ONIXLabs")
                        url.set("https://github.com/onix-labs")
                    }
                }
                scm {
                    url.set("https://github.com/onix-labs/onixlabs-kotlin")
                    connection.set("scm:git:https://github.com/onix-labs/onixlabs-kotlin.git")
                    developerConnection.set("scm:git:ssh://git@github.com/onix-labs/onixlabs-kotlin.git")
                }
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/onix-labs/onixlabs-kotlin")
            credentials {
                username = providers.gradleProperty("gpr.user").orElse(providers.environmentVariable("GITHUB_ACTOR")).orNull
                password = providers.gradleProperty("gpr.key").orElse(providers.environmentVariable("GITHUB_TOKEN")).orNull
            }
        }
    }
}
