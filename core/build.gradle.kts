plugins {
    id("java-library")
    id("io.ktor.plugin") version "3.0.0"
}

apply(plugin = "org.jetbrains.kotlin.jvm")

group = "me.vihara.atemwrapper.api"

dependencies {
    api(project(":api"))
    api(project(":protocol"))
}