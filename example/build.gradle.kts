plugins {
    id("java-library")
    id("java")
    id("io.ktor.plugin") version "3.0.0"
}


group = "me.vihara.atemwrapper.example"

dependencies {
    implementation(project(":core"))

    compileOnly("org.jetbrains:annotations:26.0.1")
    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")
}