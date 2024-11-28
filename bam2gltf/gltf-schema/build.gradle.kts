plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

dependencies {
    implementation(libs.kotlinx.serialization)
    implementation(libs.kotlinx.json)
    implementation(libs.kotlin.poet)
}

application {
    mainClass = "MainKt"
}

tasks.named("run") {
    outputs.dir("../build/generated")
}