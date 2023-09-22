val kamlVersion: String by project
val yamlPathVersion: String by project

plugins {
    kotlin("plugin.serialization")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.2")

    implementation("io.burt:jmespath-jackson:0.5.1")

    implementation("org.apache.commons:commons-lang3:3.13.0")

    testImplementation("uk.org.webcompere:model-assert:1.0.0")
}

sourceSets {
    create("moduleTest") {
        withConvention(org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet::class) {
            kotlin.srcDir("$projectDir/src/moduleTest/kotlin")
            resources.srcDir("$projectDir/src/moduleTest/resources")
            compileClasspath += sourceSets["main"].output + sourceSets["test"].output + configurations["testRuntimeClasspath"]
            runtimeClasspath += output + compileClasspath + sourceSets["test"].runtimeClasspath
        }
    }
}

task<Test>("moduleTest") {
    description = "Runs the module tests"
    group = "verification"
    testClassesDirs = sourceSets["moduleTest"].output.classesDirs
    classpath = sourceSets["moduleTest"].runtimeClasspath

    // Set tags for cucumber on which need to be filtered, use and/or to combine tags
    if (System.getProperty("cucumber.filter.tags").isNullOrEmpty()) {
        systemProperty("cucumber.filter.tags", "not @Ignore")
    } else {
        systemProperty("cucumber.filter.tags", System.getProperty("cucumber.filter.tags") + " and not @Ignore")
    }

    mustRunAfter(tasks["test"])
    useJUnitPlatform()

    testLogging {
        events("skipped", "failed")
        showStackTraces = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

