package io.datapith.jayOverlay.gradle.plugin

import io.datapit.jayOverlay.Overlay
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.name
import kotlin.io.path.writeText
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

@Suppress("UnnecessaryAbstractClass")
abstract class ApplyOverlay : DefaultTask() {

    init {
        description = "Apply overlay to json/yaml file"

        // Don't forget to set the group here.
        group = JAY_OVERLAY_GROUP
    }

    /**
     * File at which we want to apply the overlay
     */
    @get:Input
    @get:Option(option = "targetFile", description = "File at which we want to apply the overlay")
    abstract val targetFile: Property<String>

    /**
     * Overlay file we would like to apply
     */
    @get:Input
    @get: Option(option = "overlayFile", description = "Overlay file we would like to apply")
    abstract val overlayFile: Property<String>

    /**
     * Directory where result will be saved
     */
    @get:Input
    @get:Option(option = "outputDir", description = "Directory where result will be saved")
    abstract val outputDir: Property<String>

    @TaskAction
    fun execute() {
        logger.info("applying overlay ${overlayFile.get()} to ${targetFile.get()}")
        val pathOverlayFile = Paths.get(overlayFile.get())
        val pathTargetFile = Paths.get(targetFile.get())

        logger.info("Start applying overlay $pathOverlayFile to $pathTargetFile")
        val result = Overlay.apply(pathOverlayFile, pathTargetFile)

        val pathOutputDir = Paths.get(outputDir.get()).toAbsolutePath().createDirectories()
        logger.info("Output directory is ${pathOutputDir.toAbsolutePath().toFile().path}")
        val resultFile = Paths.get(pathOutputDir.toAbsolutePath().toFile().path,pathTargetFile.name)
        logger.info("Writing result of overlay\n$result\nto $resultFile")
        resultFile.createFile().writeText(result)
    }

}
