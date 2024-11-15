dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

pluginManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "bam-file-reader"
include(":library")
include(":app")