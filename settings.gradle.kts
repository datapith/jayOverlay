
pluginManagement {
    val detektPluginVersion: String by settings
    val dokkaPluginVersion: String by settings
    val kotlinVersion: String by settings
    val gradlePluginPublishVersion: String by settings
    val nexusPublishingPluginVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion

        // Plugins for relasing to maven central
        id("org.jetbrains.dokka").version(dokkaPluginVersion)
        id("io.github.gradle-nexus.publish-plugin").version(nexusPublishingPluginVersion)

        // Plugin quality check code
        id("io.gitlab.arturbosch.detekt").version(detektPluginVersion)

        // Plugin to publish Gradle plugin
        id("com.gradle.plugin-publish").version(gradlePluginPublishVersion)
    }

    repositories {
        mavenCentral()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
        gradlePluginPortal()
    }
}

include("core")
include("plugins:gradle-plugin")
//include("plugins:examples")

// always good to nail down the root project name, because the root directory name may be different in some envs (e.g. CI)
rootProject.name = "jayOverlay"

// Change names of sub modules
project(":core").name = "${rootProject.name}-core"
project(":plugins:gradle-plugin").name = "${rootProject.name}-gradle-plugin"
