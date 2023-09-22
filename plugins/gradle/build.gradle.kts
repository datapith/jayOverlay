plugins {
    `java-gradle-plugin`
    kotlin("jvm")
    id("com.gradle.plugin-publish")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(gradleApi())

    implementation(project(":core"))

    testImplementation("uk.org.webcompere:model-assert:1.0.0")
}

sourceSets {
    create("functionalTest") {
        withConvention(org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet::class) {
            kotlin.srcDir("$projectDir/src/functionalTest/kotlin")
            resources.srcDir("$projectDir/src/functionalTest/resources")
            compileClasspath += sourceSets["main"].output + configurations["testRuntimeClasspath"]
            runtimeClasspath += output + compileClasspath
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

gradlePlugin {
    plugins {
        website.set(property("WEBSITE").toString())
        vcsUrl.set(property("VCS_URL").toString())
        create(property("ID").toString()) {
            id = property("ID").toString()
            implementationClass = property("IMPLEMENTATION_CLASS").toString()
            //version = project.version
            displayName = property("DISPLAY_NAME").toString()
            description = property("DESCRIPTION").toString()
            tags.set(
                listOf(
                    "yaml",
                    "json",
                    "openapi",
                    "overlay"
                )
            )
        }
    }
}
