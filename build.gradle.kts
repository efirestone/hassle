import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import javax.xml.parsers.DocumentBuilderFactory

val kermitVersion: String by project
val ktorVersion: String by project

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.8.20")
    }
}

plugins {
    kotlin("multiplatform") version "1.9.10"
    kotlin("plugin.serialization") version "1.9.10"
    id("com.vanniktech.maven.publish") version "0.25.3"
    id("io.gitlab.arturbosch.detekt") version "1.23.0"
    id("org.jetbrains.kotlinx.kover") version "0.7.4"
    id("org.jlleitschuh.gradle.ktlint") version "11.5.0"
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
                implementation("org.jetbrains.kotlinx:atomicfu:0.22.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
                implementation("org.slf4j:slf4j-simple:2.0.7")
                api("co.touchlab:kermit:$kermitVersion")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("reflect"))
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
    delete = setOf(
        layout.buildDirectory.dir("dokka"),
    )
}

tasks.withType<DokkaTask>().configureEach {
    dependsOn("cleanDokka")
}

// Taken from https://bitspittle.dev/blog/2022/koverbadge
tasks.register("printLineCoverage") {
    group = "verification" // Put into the same group as the `kover` tasks
    dependsOn("koverXmlReport")
    doLast {
        val report = layout.buildDirectory.file("/reports/kover/report.xml")

        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(report.get().asFile)
        val rootNode = doc.firstChild
        var childNode = rootNode.firstChild

        var coveragePercent = 0.0

        while (childNode != null) {
            if (childNode.nodeName == "counter") {
                val typeAttr = childNode.attributes.getNamedItem("type")
                if (typeAttr.textContent == "LINE") {
                    val missedAttr = childNode.attributes.getNamedItem("missed")
                    val coveredAttr = childNode.attributes.getNamedItem("covered")

                    val missed = missedAttr.textContent.toLong()
                    val covered = coveredAttr.textContent.toLong()

                    coveragePercent = (covered * 100.0) / (missed + covered)

                    break
                }
            }
            childNode = childNode.nextSibling
        }

        println("%.1f".format(coveragePercent))
    }
}

tasks {
    check {
        dependsOn(test)
        finalizedBy(
            koverHtmlReport,
        )
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

ktlint {
    version.set("0.50.0")
    ignoreFailures.set(false)
}

val compileKotlinNative: KotlinNativeCompile by tasks
compileKotlinNative.apply {
    kotlinOptions.freeCompilerArgs += listOf("-opt-in=kotlin.RequiresOptIn")
}
val compileTestKotlinNative: KotlinNativeCompile by tasks
compileTestKotlinNative.apply {
    kotlinOptions.freeCompilerArgs += listOf("-opt-in=kotlin.RequiresOptIn")
}
