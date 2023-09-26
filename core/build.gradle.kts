val commonsLangVersion: String by project
val jacksonVersion: String by project
val jmeshpathVersion: String by project
val kotlinxSerializationVersion: String by project
val yamlPathVersion: String by project

plugins {
    `maven-publish`
    kotlin("plugin.serialization")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")

    implementation("io.burt:jmespath-jackson:$jmeshpathVersion")

    implementation("org.apache.commons:commons-lang3:$commonsLangVersion")
}

sourceSets {
    create("functionalTest") {
        withConvention(org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet::class) {
            kotlin.srcDir("$projectDir/src/functionalTest/kotlin")
            resources.srcDir("$projectDir/src/functionalTest/resources")
            compileClasspath += sourceSets["main"].output + sourceSets["test"].output + configurations["testRuntimeClasspath"]
            runtimeClasspath += output + compileClasspath + sourceSets["test"].runtimeClasspath
        }
    }
}

task<Test>("functionalTest") {
    description = "Runs the functional tests"
    group = "verification"
    testClassesDirs = sourceSets["functionalTest"].output.classesDirs
    classpath = sourceSets["functionalTest"].runtimeClasspath

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

