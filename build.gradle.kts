import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.repositories

plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.0.0-beta4" apply false
    kotlin("jvm") version "2.1.0" apply false
    id("maven-publish")
    id("signing")
}

val buildDirectory = file("./build/")

subprojects {
    extra["output"] = findProperty("gradle.output") ?: "./output/"

    group = "io.github.thevihara.atemwrapper"
    version = "0.0.1-SNAPSHOT"

    val customBuildDir: DirectoryProperty = project.layout.buildDirectory
    customBuildDir.set(file("$buildDirectory/${project.name}"))

    apply(plugin = "java-library")
    apply(plugin = "com.gradleup.shadow")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    repositories {
        mavenCentral()
        mavenLocal()
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
            targetCompatibility = JavaVersion.VERSION_23.toString()
            sourceCompatibility = JavaVersion.VERSION_23.toString()
            options.encoding = "UTF-8"
        }

        named("build") {
            dependsOn("shadowJar")
        }
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])

                pom {
                    name.set("Atem Wrapper")
                    description.set("A library for Atem Wrapper functionalities")
                    url.set("https://github.com/vihara/atem-wrapper")

                    licenses {
                        license {
                            name.set("Apache License 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.html")
                        }
                    }

                    developers {
                        developer {
                            id.set("vihara")
                            name.set("Vihara")
                            email.set("viharabanana@gmail.com")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/vihara/atem-wrapper.git")
                        developerConnection.set("scm:git:ssh://github.com/vihara/atem-wrapper.git")
                        url.set("https://github.com/vihara/atem-wrapper")
                    }
                }
            }
        }

        repositories {
/*            maven {
                name = "ossrh"
                url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")

                credentials {
                    username = findProperty("ossrhUsername") as String? ?: ""
                    password = findProperty("ossrhPassword") as String? ?: ""
                }
            }*/

            maven {
                name = "local"
                url = uri("file://${System.getProperty("user.home")}/.m2/repository")
            }
        }
    }

/*    signing {
        sign(publishing.publications["mavenJava"])
    }*/
}
