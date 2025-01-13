plugins {
    id("java-library")
    id("io.ktor.plugin") version "3.0.0"
}

apply(plugin = "org.jetbrains.kotlin.jvm")

group = "me.vihara.atemwrapper.protocol"

dependencies {
    api("io.ktor:ktor-server-netty")
}