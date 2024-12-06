plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

dependencies {
    implementation(libs.kotlinx.serialization)
    implementation(libs.kotlinx.json)
    implementation(libs.joml)
    implementation("io.github.rand0m-cloud.bam-file-reader:library")
}

application {
    mainClass = "org.toontownkt.bam.gltf.MainKt"
}

sourceSets {
    main {
        kotlin {
            srcDir("../build/generated")
        }
    }
}

tasks.named("compileKotlin") {
    dependsOn(":gltf-schema:run")
}

