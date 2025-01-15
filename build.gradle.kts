import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
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

    apply(plugin = "java-library")
    apply(plugin = "com.gradleup.shadow")

    repositories {
        mavenCentral()
    }

    tasks {
        withType<ShadowJar> {
            exclude("META-INF/DEPENDENCIES")
            exclude("META-INF/LICENSE")
            exclude("META-INF/LICENSE.txt")
            exclude("META-INF/NOTICE")
            exclude("META-INF/NOTICE.txt")
            exclude("META-INF/maven/**")

            mergeServiceFiles {
                include("META-INF/impl/**")
            }
        }

        withType<JavaCompile> {
            /*
                targetCompatibility = JavaVersion.VERSION_23.toString()
                sourceCompatibility = JavaVersion.VERSION_23.toString()
            */
            options.encoding = "UTF-8"
        }

        named("build") {
            dependsOn("shadowJar")
        }
    }
}