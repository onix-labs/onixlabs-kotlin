plugins {
    id("onixlabs.kotlin-library")
}

description = "Object-to-object projection, as a graph plugin."

dependencies {
    api(project(":graph"))
}
