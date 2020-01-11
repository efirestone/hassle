
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.50"
    id("org.jetbrains.dokka") version "0.9.18"
    `maven-publish`
    id("io.gitlab.arturbosch.detekt") version "1.0.0-RC16"
    id("org.jlleitschuh.gradle.ktlint") version "9.0.0"
    id("de.jansauer.printcoverage") version "2.0.0"
    jacoco
    id("com.github.dawnwords.jacoco.badge") version "0.1.0"
}

group = "com.dennisschroeder"
version = "0.1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenLocal()
    jcenter()
    maven { url = uri("https://kotlin.bintray.com/ktor") }
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
    jcenter() { url = uri("https://dl.bintray.com/kotlin/dokka") }
}

val ktorVersion: String by project
val koinVersion: String by project
val mockkVersion: String by project
val jupiterVersion: String by project
val assertVersion: String by project
val dataBobVersion: String by project
val jsonAssertVersion: String by project

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    compile("io.ktor:ktor-client-core:$ktorVersion")
    compile("io.ktor:ktor-client-core-jvm:$ktorVersion")
    compile("io.ktor:ktor-client-cio:$ktorVersion")
    compile("io.ktor:ktor-client-json-jvm:$ktorVersion")
    compile("io.ktor:ktor-client-gson:$ktorVersion")
    compile("org.koin:koin-core:$koinVersion")
    compile("org.slf4j:slf4j-simple:1.7.26")
    compile("io.github.microutils:kotlin-logging:1.6.24")
    testCompile("org.koin:koin-test:$koinVersion") {
        exclude(group = "org.mockito")
        exclude(group = "junit")
    }
    testImplementation("io.mockk:mockk:$mockkVersion")
    compile("org.junit.jupiter:junit-jupiter-api:$jupiterVersion")
    testCompile("com.willowtreeapps.assertk:assertk-jvm:$assertVersion")
    testCompile("org.skyscreamer:jsonassert:$jsonAssertVersion")

    testImplementation("io.github.daviddenton:databob.kotlin:$dataBobVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

val dokka by tasks.getting(DokkaTask::class) {
    outputFormat = "html"
    outputDirectory = "docs"
}

defaultTasks("dokka")

val sourcesJar by tasks.registering(Jar::class) {
    classifier = "sources"
    from(sourceSets["main"].allSource)
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class.java) {
            from(components["java"])
            artifact(sourcesJar.get())
        }
    }
}

tasks.withType<Test> {
    environment["HOST"] = "home-assistant.local"
    environment["PORT"] = 8321
    environment["ACCESS_TOKEN"] = "dsq7zht54899dhz43kbv4dgr56a8we234h>!sg?x"
    environment["SECURE"] = true
    environment["START_STATE_STREAM"] = false
    environment["LOG_LEVEL"] = "TRACE"
    environment["LOG_TIME"] = false
    environment["LOG_TIME_FORMAT"] = "yyy-MM-dd"
    environment["LOG_OUTPUT"] = "/khome.log"
    useJUnitPlatform()
}

tasks {
    check {
        dependsOn(test)
        finalizedBy(jacocoTestReport, jacocoTestCoverageVerification, printCoverage, generateJacocoBadge)
    }

    jacocoTestReport {
        reports {
            xml.isEnabled = true
            csv.isEnabled = false
            html.isEnabled = true
        }
    }
}

detekt {
    input = files("$projectDir/src/main/kotlin")
    config = files("$projectDir/config/detekt-config.yml")
}

ktlint {
    version.set("0.22.0")
    ignoreFailures.set(false)
}

jacoco {
    toolVersion = "0.8.4"
}

printcoverage {
    coverageType.set("LINE")
}