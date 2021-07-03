import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

// pluginManagement {
//    repositories {
//        google()
//        gradlePluginPortal()
//        mavenCentral()
//    }
// }

plugins {
    kotlin("multiplatform") version "1.5.20"
    kotlin("plugin.serialization") version "1.5.0"
    id("org.jetbrains.dokka") version "1.4.32"
    `maven-publish`
    id("io.gitlab.arturbosch.detekt") version "1.9.1"
    id("org.jlleitschuh.gradle.ktlint") version "10.1.0"
    id("de.jansauer.printcoverage") version "2.0.0"
    jacoco
    id("com.github.dawnwords.jacoco.badge") version "0.2.0"
}

group = "com.dennisschroeder"
version = "0.1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

val assertVersion: String by project
val jsonAssertVersion: String by project
val jupiterVersion: String by project
val kermitVersion: String by project
val ktorVersion: String by project
val koinVersion: String by project
val mockkVersion: String by project

// dependencies {
//    implementation(kotlin("stdlib-jdk8"))
//    // kotlinx.datetime doesn't include LocalTime yet, so supplement it
//    // https://github.com/Kotlin/kotlinx-datetime/issues/57
//    implementation("io.fluidsonic.time:fluid-time:0.14.0")
//    implementation("io.ktor:ktor-client-cio:$ktorVersion")
//    implementation("io.ktor:ktor-client-core:$ktorVersion")
//    implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
//    implementation("io.ktor:ktor-client-json-jvm:$ktorVersion")
//    implementation("io.ktor:ktor-client-serialization:$ktorVersion")
//    implementation("io.insert-koin:koin-core:$koinVersion")
//    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.2.1")
//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
//    implementation("org.slf4j:slf4j-simple:1.7.30")
//    implementation(kotlin("stdlib-common"))
//    api("co.touchlab:kermit:$kermitVersion")
//    testImplementation("io.insert-koin:koin-test:$koinVersion") {
//        exclude(group = "org.mockito")
//        exclude(group = "junit")
//    }
//    testImplementation("io.mockk:mockk:$mockkVersion")
//    implementation("org.junit.jupiter:junit-jupiter-api:$jupiterVersion")
//    testImplementation("com.willowtreeapps.assertk:assertk-jvm:$assertVersion")
//    testImplementation("org.skyscreamer:jsonassert:$jsonAssertVersion")
//
//    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")
// }

tasks.create<Delete>("cleanDokka") {
    delete = setOf("$rootDir/docs/khome")
}

tasks {
    dokkaHtml {
        dependsOn("cleanDokka")
        outputDirectory.set(File("$rootDir/docs"))
    }
}

defaultTasks("dokkaHtml")

repositories {
    mavenCentral()

//    mavenLocal()
//    mavenCentral()
//    google()
//    maven { url = uri("https://kotlin.bintray.com/ktor") }
//    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xjsr305=strict", "-Xopt-in=kotlin.RequiresOptIn")
                jvmTarget = "1.8"
            }
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }
//    macosX64 {
//        compilations.all {
//            kotlinOptions {
//                freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
//            }
//            depend
//        }
//    }
    val hostOs = System.getProperty("os.name")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                // kotlinx.datetime doesn't include LocalTime yet, so supplement it
                // https://github.com/Kotlin/kotlinx-datetime/issues/57
                implementation("io.fluidsonic.time:fluid-time:0.14.0")
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-serialization:$ktorVersion")
                implementation("io.insert-koin:koin-core:$koinVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.2.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
                implementation("org.slf4j:slf4j-simple:1.7.30")
                api("co.touchlab:kermit:$kermitVersion")
                implementation(kotlin("stdlib-common"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
//        val iosMain by getting
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
                implementation("io.ktor:ktor-client-json-jvm:$ktorVersion")
//                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.5.0")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter-api:$jupiterVersion")
//                testImplementation("io.insert-koin:koin-test:$koinVersion") {
//                    exclude(group = "org.mockito")
//                    exclude(group = "junit")
//                }
//                testImplementation("io.mockk:mockk:$mockkVersion")
//
//                testImplementation("com.willowtreeapps.assertk:assertk-jvm:$assertVersion")
//                testImplementation("org.skyscreamer:jsonassert:$jsonAssertVersion")
//
//                testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")
            }
        }
        val nativeMain by getting {
            dependencies {
//                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:1.3.8")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-macosx64:1.5.0-native-mt") {
                    // https://youtrack.jetbrains.com/issue/KT-41378
                    version { strictly("1.5.0-native-mt") }
                }
            }
        }
        val nativeTest by getting
    }
}

val compileKotlinNative: KotlinNativeCompile by tasks
compileKotlinNative.apply {
    kotlinOptions.freeCompilerArgs += listOf("-Xopt-in=kotlin.RequiresOptIn")
}

//
// //repositories {
// //    mavenLocal()
// //    mavenCentral()
// //    google()
// //    maven { url = uri("https://kotlin.bintray.com/ktor") }
// //    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
// //}
//
//
// kotlin {
//    jvm()
//    sourceSets {
//        val commonMain by getting {
//            dependencies {
//                implementation(kotlin("stdlib-jdk8"))
//                implementation("io.ktor:ktor-client-core:$ktorVersion")
//                implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
//                implementation("io.ktor:ktor-client-cio:$ktorVersion")
//                implementation("io.ktor:ktor-client-json-jvm:$ktorVersion")
//                implementation("io.ktor:ktor-client-gson:$ktorVersion")
//                implementation("io.insert-koin:koin-core:$koinVersion")
//                implementation("org.slf4j:slf4j-simple:1.7.30")
//                implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")
//                testImplementation("io.insert-koin:koin-test:$koinVersion") {
//                    exclude(group = "org.mockito")
//                    exclude(group = "junit")
//                }
//                testImplementation("io.mockk:mockk:$mockkVersion")
//                implementation("org.junit.jupiter:junit-jupiter-api:$jupiterVersion")
//                testImplementation("com.willowtreeapps.assertk:assertk-jvm:$assertVersion")
//                testImplementation("org.skyscreamer:jsonassert:$jsonAssertVersion")
//
//                testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")
//            }
//        }
//    }
// }

//
// val sourcesJar by tasks.registering(Jar::class) {
//    archiveClassifier.set("sources")
//    from(sourceSets["main"].allSource)
// }
//
// publishing {
//    publications {
//        register("mavenJava", MavenPublication::class.java) {
//            from(components["java"])
//            artifact(sourcesJar.get())
//        }
//    }
// }
//
// tasks.withType<Test> {
//    environment["HOST"] = "home-assistant.local"
//    environment["PORT"] = 8321
//    environment["ACCESS_TOKEN"] = "dsq7zht54899dhz43kbv4dgr56a8we234h>!sg?x"
//    environment["SECURE"] = true
//    environment["START_STATE_STREAM"] = false
//    useJUnitPlatform()
// }
//
// tasks {
//    check {
//        dependsOn(test)
//        finalizedBy(jacocoTestReport, jacocoTestCoverageVerification, printCoverage, generateJacocoBadge)
//    }
//
//    jacocoTestReport {
//        reports {
//            xml.required.set(true)
//            csv.required.set(false)
//            html.required.set(true)
//        }
//    }
// }
//
// detekt {
//    input = files("$projectDir/src/main/kotlin")
//    config = files("$projectDir/config/detekt-config.yml")
// }
//
// ktlint {
//    version.set("0.41.0")
//    ignoreFailures.set(false)
// }
//
// jacoco {
//    toolVersion = "0.8.7"
// }
//
// printcoverage {
//    coverageType.set("LINE")
// }
