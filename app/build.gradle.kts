plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":library"))
    implementation(libs.clikt)
    implementation(libs.kotlinx.json)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(20)
}


application {
    mainClass = "org.toontownkt.bam.app.MainKt"
}

tasks.jar {
    manifest {
        attributes("Main-Class" to application.mainClass)
    }
}
