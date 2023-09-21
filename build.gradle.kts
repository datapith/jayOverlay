val cucumberVersion: String by project
val detektPluginVersion: String by project
val javaVersion: String by project
val junitVersion: String by project
val kotlinLoggingVersion: String by project
val logbackVersion: String by project
val mockkVersion: String by project

plugins {
    jacoco
    kotlin("jvm")
    id("io.gitlab.arturbosch.detekt")
}

allprojects {
    group = "io.datapith.plugins"

    repositories {
        mavenCentral()
    }
}

group = "io.datapit.jayOverlay"
version = "0.1.0"

subprojects {

    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("jacoco")
        plugin("io.gitlab.arturbosch.detekt")
    }

    kotlin {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of(javaVersion))
        }
    }

    detekt {
        autoCorrect = true
        debug = false
        ignoreFailures = false
        config = files("$rootDir/detekt.yml")
    }

    repositories {
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:$detektPluginVersion")

        implementation("ch.qos.logback", "logback-classic", logbackVersion)
        implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")

        testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

        testImplementation("io.mockk:mockk:$mockkVersion")

        // Dependencies for module tests
        testImplementation("io.cucumber:cucumber-core:$cucumberVersion")
        testImplementation("io.cucumber:cucumber-java:$cucumberVersion")
        testImplementation("io.cucumber:cucumber-junit:$cucumberVersion")
        testRuntimeOnly("org.junit.vintage:junit-vintage-engine:$junitVersion")
    }

    tasks.test {
        // Discover and execute JUnit Platform-based (JUnit 5, JUnit Jupiter) tests
        useJUnitPlatform()
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.test) // tests are required to run before generating the report
        reports {
            xml.required.set(true)
            csv.required.set(false)
            html.required.set(true)
        }
    }
}
