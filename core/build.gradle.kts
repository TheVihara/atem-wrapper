plugins {
    id("java-library")
    id("io.ktor.plugin") version "3.0.3"
}

apply(plugin = "org.jetbrains.kotlin.jvm")

group = "me.vihara.atemwrapper.core"

dependencies {
    api(project(":api"))
    api(project(":protocol"))
}