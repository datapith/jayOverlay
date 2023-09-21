
pluginManagement {
    val detektPluginVersion: String by settings
    val kotlinVersion: String by settings
    val gradlePluginPublishVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion

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
include("plugins:gradle")
//include("plugins:examples")
