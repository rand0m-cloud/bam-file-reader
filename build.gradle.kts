import org.jreleaser.model.Active
import org.jreleaser.model.Signing

group = "io.github.rand0m-cloud.bam-file-reader"
version = "0.1"

val stagingDeploy = rootProject.layout.buildDirectory.dir("staging-deploy")

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization) apply false

    alias(libs.plugins.jreleaser)
    `maven-publish`
    signing
    alias(libs.plugins.shadow) apply false
}

allprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = rootProject.group
    version = rootProject.version

    publishing {
        repositories {
            maven {
                url = uri(stagingDeploy)
            }
        }
    }

    java {
        withJavadocJar()
        withSourcesJar()
    }
}

jreleaser {
    strict = true

    project {
        description = "A Kotlin project for using .bam files from the Panda3D engine."
        copyright = "2024"
    }

    signing {
        active = Active.ALWAYS
        armored = true
        mode = Signing.Mode.COMMAND
        command {
            keyName = "3F702352828D85E5A456DB86159B0816973D1C2E"
        }
    }

    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    active = Active.ALWAYS
                    url = "https://central.sonatype.com/api/v1/publisher"
                    stagingRepository("build/staging-deploy")
                }
            }
        }
    }
}


val cleanStagingMaven by tasks.registering(Delete::class) {
    delete(stagingDeploy)
}

tasks.register("setupStagingMaven") {
    dependsOn(cleanStagingMaven, ":library:compileKotlin")
    finalizedBy(":library:publishMavenPublicationToMavenRepository")
}

tasks.register("releaseLibrary") {
    dependsOn("setupStagingMaven")
    finalizedBy("jreleaserDeploy")
}