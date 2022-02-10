import com.vanniktech.maven.publish.JavadocJar.Dokka
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

val kermitVersion: String by project
val ktorVersion: String by project

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.6.10")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.18.0")
    }
}

plugins {
    kotlin("multiplatform") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("com.github.dawnwords.jacoco.badge") version "0.2.0"
    id("de.jansauer.printcoverage") version "2.0.0"
    id("io.gitlab.arturbosch.detekt") version "1.9.1"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
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
                freeCompilerArgs = listOf("-Xjsr305=strict", "-Xopt-in=kotlin.RequiresOptIn")
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
                implementation("org.jetbrains.kotlinx:atomicfu:0.17.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")
                implementation("org.slf4j:slf4j-simple:1.7.32")
                api("co.touchlab:kermit:$kermitVersion")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
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
            generateJacocoBadge
        )
    }
    jacocoTestReport {
        val coverageSourceDirs = arrayOf(
            "$rootDir/src/commonMain",
            "$rootDir/src/jvmMain"
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

configure<MavenPublishBaseExtension> {
    version = System.getenv("VERSION")
    configure(
        KotlinMultiplatform(javadocJar = Dokka("dokkaHtml"))
    )
}

detekt {
    input = files("$projectDir/src/main/kotlin")
    config = files("$projectDir/config/detekt-config.yml")
}

jacoco {
    toolVersion = "0.8.7"
}

ktlint {
    version.set("0.42.1")
    ignoreFailures.set(false)
}

printcoverage {
    coverageType.set("LINE")
}

val compileKotlinNative: KotlinNativeCompile by tasks
compileKotlinNative.apply {
    kotlinOptions.freeCompilerArgs += listOf("-Xopt-in=kotlin.RequiresOptIn")
}
val compileTestKotlinNative: KotlinNativeCompile by tasks
compileTestKotlinNative.apply {
    kotlinOptions.freeCompilerArgs += listOf("-Xopt-in=kotlin.RequiresOptIn")
}
