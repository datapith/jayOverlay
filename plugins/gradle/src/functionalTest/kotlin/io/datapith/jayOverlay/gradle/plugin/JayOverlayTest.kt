package io.datapith.jayOverlay.gradle.plugin

import java.io.File
import java.nio.file.Paths
import mu.two.KotlinLogging
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Test
import org.junit.rules.TemporaryFolder
import uk.org.webcompere.modelassert.json.JsonAssertions.assertJson

class JayOverlayTest {
    private val logger = KotlinLogging.logger {}

    // creates temp directory for a gradle project
    private val testProjectDir = TemporaryFolder()

    private lateinit var buildFile: File
    private lateinit var gradleRunner: GradleRunner

    private lateinit var overlayFile: File
    private lateinit var targetFile: File

    @Before
    fun setup() {

        testProjectDir.create()

        // Create yaml file for testing overlay
        targetFile = testProjectDir.newFile("targetFile.json")
        targetFile.writeText(
            """
                 {
                    "openapi": "3.0.0",
                    "info": {
                        "version": "1.0.0",
                        "title": "API"
                    },
                    "paths": {}
                 }           
                 """
        )

        overlayFile = testProjectDir.newFile("overlayFile.json")
        overlayFile.writeText(
            """
                {
                    "overlay": "0.1.0",
                    "info": {
                    "title": "Adding description overlay",
                    "version": "1.0.0"
                },
                    "actions": [
                        {
                            "target": "paths",
                            "description": "Adding path to openapi specification",
                            "update": {
                                "/list" : {
                                    "get" : {
                                        "description": "Returns a list of stuff",             
                                        "responses": {
                                            "200": {
                                                "description": "Successful response"                                
                                            }
                                        }
                                    }
                                }
                            }
                        }    
                    ]
                }
                """
        )

        // create gradle build file in the test gradle project
        buildFile = testProjectDir.newFile("build.gradle.kts")
        // add common configuration for all tests in this class
        buildFile.appendText(
            """
            plugins {
                id("io.datapith.jayOverlay")
            }
            
            jayOverlay {
                targetFile.set("${targetFile.path}")
                overlayFile.set("${overlayFile.path}")
                outputDir.set("${testProjectDir.root.path}/result")
            }
            """.trimIndent()
        )

        // creates and configures gradle runner
        gradleRunner = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withTestKitDir(testProjectDir.newFolder())
    }

    @Test
    fun `check setup gradle build`() {
        // runs `tasks` gradle task
        val result = gradleRunner
            .withArguments("help", "--task", "applyOverlay")
            .build()
        println(result.output)
    }

    @Test
    fun `apply overlay to json file`() {
        val outputBuild = gradleRunner
            //.withDebug(true)
            .withArguments(
                "--i",
                "applyOverlay"
            )
            .build()

        // Get overlay file and compare to expected result
        val result = Paths.get(testProjectDir.toString(), targetFile.name).toFile().readText()
        assertJson(result).isEqualToYaml(
            """
                openapi: "3.0.0"
                info:
                  version: "1.0.0"
                  title: "API"
                paths: 
                    /list:
                        get:
                            description: Returns a list of stuff              
                            responses:
                                "200":
                                    description: Successful response                            
            """
        )

        println(outputBuild.output)
    }
}
