import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Shared Kotlin/JVM setup for every project in the build, published or not.
// Compiles with the JDK 25 toolchain, but emits JVM 17 bytecode against the JDK 17 API.

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("com.diffplug.spotless")
}

val libs = versionCatalogs.named("libs")
val buildJdk = 25
val targetJdk = 17

kotlin {
    jvmToolchain(buildJdk)
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(targetJdk.toString()))
        freeCompilerArgs.add("-Xjdk-release=$targetJdk")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(targetJdk)
}

dependencies {
    "implementation"(libs.findLibrary("kotlin-reflect").get())
    "testImplementation"(kotlin("test"))
    "testImplementation"(platform(libs.findLibrary("junit-bom").get()))
    "testRuntimeOnly"(libs.findLibrary("junit-platform-launcher").get())
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()

    // Run the tests on another JDK with -PtestJdk=17, without changing the build JDK.
    providers.gradleProperty("testJdk").orNull?.let { version ->
        javaLauncher.set(javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(version)) })
    }
}

spotless {
    kotlin {
        target("src/**/*.kt")
        licenseHeaderFile(rootProject.file("HEADER"))
    }
}
