plugins {
    id("java-library")
    id("io.ktor.plugin") version "3.0.3"
}

apply(plugin = "org.jetbrains.kotlin.jvm")

group = "io.github.thevihara.atemwrapper.protocol"

dependencies {
    api("io.ktor:ktor-server-netty")
}