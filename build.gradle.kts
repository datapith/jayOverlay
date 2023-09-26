import java.net.URI

val cucumberVersion: String by project
val detektPluginVersion: String by project
val javaVersion: String by project
val junitVersion: String by project
val kotlinLoggingVersion: String by project
val logbackVersion: String by project
val mockkVersion: String by project
val webcompereVersion: String by project

val projectUrl: String by project
val projectDescription: String by project

// Settings source control
val scmConnection: String by project
val scmDeveloperConnection: String by project
val scmUrl: String by project

// properties for signing artifacts
val signingKey: String? by project
val signingPassword: String? by project

// Credentials for publishing artifacts to maven central
val ossrhUsername: String? by project
val ossrhPassword: String? by project

plugins {
    // Plugins required for publishing artifacts maven central
    `maven-publish`
    // todo use id("io.github.gradle-nexus.publish-plugin").version("1.0.0")
    // see https://dev.to/madhead/no-bullshit-guide-on-publishing-your-gradle-projects-to-maven-central-3ok4
    signing
    id("org.jetbrains.dokka")
    //id("io.github.gradle-nexus.publish-plugin")

    jacoco
    kotlin("jvm")
    id("io.gitlab.arturbosch.detekt")
}

allprojects {
    group = "io.datapith.jayOverlay"

    repositories {
        mavenCentral()
    }
}

subprojects {

    val isReleaseVersion = !version.toString().endsWith("SNAPSHOT")

    apply {
        plugin("maven-publish")
        plugin("signing")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("jacoco")
        plugin("io.gitlab.arturbosch.detekt")
    }

    // you need source and Javadoc JARs to publish on Maven Central.
    java {
        withSourcesJar()
        withJavadocJar()
    }

    publishing {

        publications {
            create<MavenPublication>("mavenJava") {
                groupId = project.group.toString()
                artifactId = project.name
                version = project.version.toString()

                from(components["java"])

                // POM metadata
                pom {
                    name.set(requireNotNull(project.name))
                    description.set(projectDescription)
                    url.set(projectUrl)
                    licenses {
                        license {
                            name.set("AGPL3")
                            url.set("https://opensource.org/license/agpl-v3/")
                        }
                    }
                    developers {
                        developer {
                            id.set("datapith")
                            name.set("Datapith")
                            email.set("info@datapith.io")
                        }
                    }
                    scm {
                        connection.set(scmConnection)
                        developerConnection.set(scmDeveloperConnection)
                        url.set(scmUrl)
                    }
                }
            }
        }

        // To really publish we should go to [staging](https://oss.sonatype.org/) and publish
        repositories {
            // Maven central
            maven {
                name = "OSSRH"

                val releaseRepo = URI.create("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                val  snapshotRepo = URI.create("https://s01.oss.sonatype.org/content/repositories/snapshots/")

                url = if (isReleaseVersion) releaseRepo else snapshotRepo
                //url = URI.create("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = ossrhUsername?:"unknownOssrhUserName"
                    password = ossrhPassword?:"unknownOssrhPassword"
                }
            }
        }
    }

    signing {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["mavenJava"])
    }
    tasks.withType(Sign::class) {
        onlyIf{ isReleaseVersion}
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

        // Asserts for YAML and JSON
        testImplementation("uk.org.webcompere:model-assert:$webcompereVersion")

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
