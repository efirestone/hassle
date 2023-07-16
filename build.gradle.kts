import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

val kermitVersion: String by project
val ktorVersion: String by project

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.8.20")
    }
}

plugins {
    kotlin("multiplatform") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
    id("com.github.dawnwords.jacoco.badge") version "0.2.4"
    id("de.jansauer.printcoverage") version "2.0.0"
    id("com.vanniktech.maven.publish") version "0.25.3"
    id("io.gitlab.arturbosch.detekt") version "1.23.0"
    id("org.jlleitschuh.gradle.ktlint") version "11.5.0"
    jacoco
    `java-library`
}

// The vanniktech publishing plugin depends on Dokka being in the classpath
// and that doesn't seem to be possible with the `plugins` block, so we need
// to use the older-style `buildscript.dependencies.classpath` and `apply` method.
apply(plugin = "com.vanniktech.maven.publish")
apply(plugin = "org.jetbrains.dokka")

java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    gradlePluginPortal()
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xjsr305=strict", "-opt-in=kotlin.RequiresOptIn")
                jvmTarget = "1.8"
            }
        }
    }
    val hostOs = System.getProperty("os.name")
    val nativeTarget = when (hostOs) {
        "Mac OS X" -> macosX64("native")
        "Linux" -> linuxX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                // kotlinx.datetime doesn't include LocalTime yet, so supplement it
                // https://github.com/Kotlin/kotlinx-datetime/issues/57
                implementation("io.fluidsonic.time:fluid-time:0.15.0")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-websockets:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("org.jetbrains.kotlinx:atomicfu:0.21.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                implementation("org.slf4j:slf4j-simple:2.0.7")
                api("co.touchlab:kermit:$kermitVersion")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-cio-jvm:$ktorVersion")
            }
        }
        val jvmTest by getting
        val nativeMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-macosx64:1.6.0-native-mt") {
                    // https://youtrack.jetbrains.com/issue/KT-41378
                    version { strictly("1.6.0-native-mt") }
                }
            }
        }
        val nativeTest by getting
    }
}

tasks.create<Delete>("cleanDokka") {
    delete = setOf("$buildDir/dokka")
}

tasks.withType<DokkaTask>().configureEach {
    dependsOn("cleanDokka")
}

tasks {
    check {
        dependsOn(test)
        finalizedBy(
            jacocoTestReport,
            jacocoTestCoverageVerification,
            printCoverage,
            generateJacocoBadge,
        )
    }
    jacocoTestReport {
        val coverageSourceDirs = arrayOf(
            "$rootDir/src/commonMain",
            "$rootDir/src/jvmMain",
        )

        val classFiles = "$buildDir/classes/kotlin/jvm/"

        classDirectories.setFrom(files(classFiles))
        sourceDirectories.setFrom(files(coverageSourceDirs))

        executionData
            .setFrom(files("$buildDir/jacoco/jvmTest.exec"))

        reports {
            xml.required.set(true)
            csv.required.set(false)
            html.required.set(true)
        }
    }
}

mavenPublishing {
    // If any issues arise with publishing, check here:
    // https://s01.oss.sonatype.org/#stagingRepositories
    version = System.getenv("VERSION")

    signAllPublications()
}

detekt {
    config.setFrom("$projectDir/config/detekt-config.yml")
}

jacoco {
    toolVersion = "0.8.10"
}

ktlint {
    version.set("0.50.0")
    ignoreFailures.set(false)
}

printcoverage {
    coverageType.set("LINE")
}

val compileKotlinNative: KotlinNativeCompile by tasks
compileKotlinNative.apply {
    kotlinOptions.freeCompilerArgs += listOf("-opt-in=kotlin.RequiresOptIn")
}
val compileTestKotlinNative: KotlinNativeCompile by tasks
compileTestKotlinNative.apply {
    kotlinOptions.freeCompilerArgs += listOf("-opt-in=kotlin.RequiresOptIn")
}
