dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}


rootProject.name = "bam2gltf"
include(":app")
include(":gltf-schema")
include("")
includeBuild("../") {
    name = "root"
}
