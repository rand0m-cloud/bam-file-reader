plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    `java-library`
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.joml)
    implementation(libs.kotlinx.serialization)
    testImplementation(libs.kotlinx.json)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    explicitApi()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = group as String
            artifactId = "core"
            version = rootProject.version as String

            from(components["java"])

            pom {
                name = "bam-file-reader"
                description = "A library for reading .bam files from the Panda3D game engine"
                url = "https://github.com/rand0m-cloud/bam-file-reader"
                organization {
                    name = "io.github.rand0m-cloud.bam-file-reader"
                    url = "https://github.com/rand0m-cloud/bam-file-reader"
                }

                developers {
                    developer {
                        name = "Abby Bryant"
                        email = "rand0m-cloud@outlook.com"
                        url = "https://github.com/rand0m-cloud"
                    }
                }

                issueManagement {
                    system = "github"
                    url = "https://github.com/rand0m-cloud/bam-file-reader"
                }

                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                        distribution = "repo"
                    }
                    license {
                        name = "The MIT License"
                        url = "https://opensource.org/license/mit"
                        distribution = "repo"
                    }
                }

                scm {
                    url = "https://github.com/rand0m-cloud/bam-file-reader"
                    connection = "scm:git:git://github.com/rand0m-cloud/bam-file-reader"
                    developerConnection = "scm:git:ssh://github.com/rand0m-cloud/bam-file-reader.git"
                }
            }
        }
    }
}