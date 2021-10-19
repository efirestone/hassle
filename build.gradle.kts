import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

val assertVersion: String by project
val kermitVersion: String by project
val ktorVersion: String by project
val mockkVersion: String by project

plugins {
    kotlin("multiplatform") version "1.5.31"
    kotlin("plugin.serialization") version "1.5.0"
    id("com.github.dawnwords.jacoco.badge") version "0.2.0"
    id("de.jansauer.printcoverage") version "2.0.0"
    id("io.gitlab.arturbosch.detekt") version "1.9.1"
    id("org.jetbrains.dokka") version "1.5.30"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
    id("java-library")
    jacoco
    `maven-publish`
}

group = "com.codellyrandom.hassemble"
version = "0.1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
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
                implementation("io.fluidsonic.time:fluid-time:0.14.0")
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-serialization:$ktorVersion")
                implementation("org.jetbrains.kotlinx:atomicfu:0.16.2")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.0")
                implementation("org.slf4j:slf4j-simple:1.7.30")
                api("co.touchlab:kermit:$kermitVersion")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
                implementation("io.ktor:ktor-client-json-jvm:$ktorVersion")
            }
        }
        val jvmTest by getting
        val nativeMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-macosx64:1.5.0-native-mt") {
                    // https://youtrack.jetbrains.com/issue/KT-41378
                    version { strictly("1.5.0-native-mt") }
                }
            }
        }
        val nativeTest by getting
    }
}

defaultTasks("dokkaHtml")

val srcsJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class.java) {
            from(components["java"])
            artifact(srcsJar.get())
        }
    }
}

tasks.create<Delete>("cleanDokka") {
    delete = setOf("$rootDir/docs/${rootProject.name}")
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
    dokkaHtml {
        dependsOn("cleanDokka")
        outputDirectory.set(File("$rootDir/docs"))
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
