plugins {
    id("java-library")
    id("io.ktor.plugin") version "3.0.3"
}

apply(plugin = "org.jetbrains.kotlin.jvm")

dependencies {
    api("io.ktor:ktor-server-netty")
}

application {
    mainClass.set("")
}