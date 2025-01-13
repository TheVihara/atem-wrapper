import org.gradle.kotlin.dsl.repositories

plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.0.0-beta4" apply false
    kotlin("jvm") version "2.0.20" apply false
}

val buildDirectory = file("./build/")

subprojects {
    extra["output"] = findProperty("gradle.output") ?: "./output/"

    group = "me.vihara.atemwrapper"
    version = "0.0.1-SNAPSHOT"

    val customBuildDir: DirectoryProperty = project.layout.buildDirectory
    customBuildDir.set(file("$buildDirectory/${project.name}"))

    apply(plugin = "com.gradleup.shadow")

    repositories {
        mavenCentral()
    }
}