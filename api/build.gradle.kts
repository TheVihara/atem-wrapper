plugins {
    id("java")
}

group = "me.vihara.atemwrapper.api"

dependencies {
    compileOnly("org.jetbrains:annotations:26.0.1")
    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")
}