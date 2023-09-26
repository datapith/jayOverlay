package io.datapith.jayOverlay.gradle.plugin

import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

/**
 * Extension object containing all the settings and properties of the plugin.
 */
@Suppress("UnnecessaryAbstractClass")
abstract class JayOverlayExtension @Inject constructor(project: Project) {

    private val projectObjects = project.objects

    init {
        println("Starting extensions")
    }

    /**
     * File at which overlay will be applied
     */
    @get:Input
    @get:Option(option = "targetFile", description = "File at which we want to apply the overlay")
    val targetFile = projectObjects.property(String::class.java).convention("test")

    /**
     * overlay file
     */
    val overlayFile = projectObjects.property(String::class.java).convention(("test"))

    /**
     * Output directory
     */
    val outputDir = projectObjects.property(String::class.java).convention("build/generated/overlay")

    @TaskAction
    fun execute() {
        // Empty because jayOverlay is a placeholder for common variables used in tasks
    }

}
