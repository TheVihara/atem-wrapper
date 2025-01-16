import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java-library")
    id("java")
    id("io.ktor.plugin") version "3.0.3"
}

val output: String = findProperty("output.path")?.toString()
    ?: "./output/"

group = "me.vihara.atemwrapper.example"

application {
    project.setProperty("mainClassName", "me.vihara.atemwrapper.example.ExampleAppEntrypoint")
}

dependencies {
    implementation(project(":core"))

    compileOnly("org.jetbrains:annotations:26.0.1")
    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")
}

tasks {
    named<Jar>("jar") {
        manifest {
            attributes["Main-Class"] = "me.vihara.atemwrapper.example.ExampleAppEntrypoint"
        }
    }

    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("ExampleApp")
        archiveClassifier.set("")
        archiveVersion.set("")
        destinationDirectory.set(file(output))
    }
}