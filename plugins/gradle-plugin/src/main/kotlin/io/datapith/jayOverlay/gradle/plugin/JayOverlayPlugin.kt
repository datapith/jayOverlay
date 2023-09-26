package io.datapith.jayOverlay.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

const val EXTENSION_NAME = "jayOverlay"
const val JAY_OVERLAY_GROUP = "overlay"
const val TASK_NAME_APPLY = "applyOverlay"

class JayOverlayPlugin: Plugin<Project> {

    /**
     * Apply this plugin to the given project.
     *
     * @param project The target project
     */
    override fun apply(project: Project) {
        // Create extension for plugin
        val extension = project.extensions.create(EXTENSION_NAME, JayOverlayExtension::class.java, project)

        // Add a task to apply overlay
        project.tasks.register(TASK_NAME_APPLY, ApplyOverlay::class.java) {
            it.targetFile.set(extension.targetFile)
            it.overlayFile.set(extension.overlayFile)
            it.outputDir.set(extension.outputDir)
        }

    }
}
