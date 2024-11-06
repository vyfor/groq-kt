@file:OptIn(org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl::class)
@file:Suppress("OPT_IN_USAGE")

import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("multiplatform") version "2.0.10"
    kotlin("plugin.serialization") version "2.0.10"
    id("org.jetbrains.dokka") version "1.9.20"
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = "io.github.vyfor"
version = "0.0.1"

repositories {
    mavenCentral()
}

kotlin {
    compilerOptions {
        jvmToolchain(8)
    }
    
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX86()
    androidNativeX64()
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    js()
    jvm()
    linuxArm64()
    linuxX64()
    macosArm64()
    macosX64()
    mingwX64()
    tvosArm64()
    tvosSimulatorArm64()
    tvosX64()
    wasmJs()
    watchosArm32()
    watchosArm64()
    watchosDeviceArm64()
    watchosSimulatorArm64()
    watchosX64()
    
    sourceSets {
        val ktorVersion = "3.0.1"
        
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
            }
        }
        
        tasks.withType<Test> {
            testLogging {
                showStandardStreams = true
            }
        }
    }
}

mavenPublishing {
    configure(
        KotlinMultiplatform(
            javadocJar = JavadocJar.Dokka("dokkaHtml"),
            sourcesJar = true
        )
    )
    
    coordinates("io.github.vyfor", "groq-kt", project.version.toString())
    
    pom {
        name.set("groq-kt")
        description.set("Kotlin Multiplatform wrapper for Groq")
        url.set("https://github.com/vyfor/groq-kt")
        inceptionYear.set("2024")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("vyfor")
                name.set("vyfor")
                url.set("https://github.com/vyfor/")
            }
        }
        scm {
            url.set("https://github.com/vyfor/groq-kt/")
            connection.set("scm:git:git://github.com/vyfor/groq-kt.git")
            developerConnection.set("scm:git:ssh://git@github.com/vyfor/groq-kt.git")
        }
        issueManagement {
            system.set("Github")
            url.set("https://github.com/vyfor/groq-kt/issues")
        }
    }
    
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    
    signAllPublications()
}
