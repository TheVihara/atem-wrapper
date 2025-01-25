plugins {
    id("java-library")
    id("io.ktor.plugin") version "3.0.3"
}

apply(plugin = "org.jetbrains.kotlin.jvm")

dependencies {
    api(project(":api"))
    api(project(":protocol"))
}

application {
    mainClass.set("")
}