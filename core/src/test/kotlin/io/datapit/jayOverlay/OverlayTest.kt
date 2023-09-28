package io.datapit.jayOverlay

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import mu.two.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.org.webcompere.modelassert.json.JsonAssertions.assertJson
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText

class OverlayTest {

    private val logger = KotlinLogging.logger {}

    val objectMapper: ObjectMapper

    init {
        val yamlFactory = YAMLFactory()
        objectMapper = ObjectMapper(yamlFactory)
    }

    @Test
    fun `overlay should fail when no actions specified`() {
        // Given overlay without any actions
        val overlay = """
                {
                    "overlay": "0.1.0",
                    "info": {
                    "title": "Adding description overlay",
                    "version": "1.0.0"
                },
                    "extends": "src/test/resources/empty.yml",
                    "actions": [
                    ]
                }
        """.trim()

        // When applying overlay
        // Then exception should be thrown
        val exception = assertThrows<IllegalArgumentException> {
            applyOverlay(overlay)
        }
        assertEquals("There must be at least one action within the overlay", exception.message)
    }

    @Test
    fun `overlay should fail when update applies at primitive node`() {
        // Given overlay without any actions
        val overlay = """
                {
                    "overlay": "0.1.0",
                    "info": {
                    "title": "Adding description overlay",
                    "version": "1.0.0"
                },
                    "extends": "src/test/resources/empty.yml",
                    "actions": [
                        {
                            "target": "info.title",
                            "update": {
                                "title": "updated title"
                            }
                        }
                    ]
                }
        """.trim()

        // When applying overlay
        // Then exception should be thrown
        val exception = assertThrows<IllegalArgumentException> {
            applyOverlay(overlay)
        }
        assertEquals("Only object nodes can be updated", exception.message)
    }

    @Test
    fun `overlay should fail when overlay targets non existing node`() {
        // Given overlay without any actions
        val overlay = """
                {
                    "overlay": "0.1.0",
                    "info": {
                    "title": "Adding description overlay",
                    "version": "1.0.0"
                },
                    "extends": "src/test/resources/empty.yml",
                    "actions": [
                        {
                            "target": "info.description",
                            "update": {
                                "title": "remove description",
                                "remove": true
                            }
                        }
                    ]
                }
        """.trim()

        // When applying overlay
        // Then exception should be thrown
        val exception = assertThrows<IllegalArgumentException> {
            applyOverlay(overlay)
        }
        assertEquals("Could not find info.description", exception.message)
    }

    @Test
    fun `overlay should fail when extends point to non existing document`() {
        // Given a overlay extending a non-existing document
        val overlay = """
                {
                    "overlay": "0.1.0",
                    "info": {
                    "title": "Adding description overlay",
                    "version": "1.0.0"
                },
                    "extends": "/non/existing/document.yaml",
                    "actions": [
                        {
                            "target": "info",
                            "description": "Adding description to openapi specification",
                            "update": {
                                "description": "Added description"
                            }
                        }    
                    ]
                }
        """.trim()

        // When applying overlay
        // Then Exception should occur
        val exception = assertThrows<java.lang.IllegalArgumentException> { applyOverlay(overlay) }
        assertEquals("Document /non/existing/document.yaml does not exist", exception.message)
    }

    @Test
    fun `overlay should add Node to yaml file`() {
        // Given an open API specification without description
        val targetDocument = """
                openapi: "3.0.0"
                info:
                  version: "1.0.0"
                  title: "API"
                paths: {}            
            """.trimIndent()

        // And overlay by which we add description
        val overlay = """
                {
                    "overlay": "0.1.0",
                    "info": {
                    "title": "Adding description overlay",
                    "version": "1.0.0"
                },
                    "actions": [
                        {
                            "target": "info",
                            "description": "Adding description to openapi specification",
                            "update": {
                                "description": "Added description"
                            }
                        }    
                    ]
                }
        """.trim()

        // When applying overlay to target document
        val result = applyOverlay(overlay, targetDocument)

        // Then result of overlay is that description is added
        val returnedYaml = objectMapper.readTree(result)
        assertJson(returnedYaml).isEqualToYaml(
            """
                openapi: "3.0.0"
                info:
                  version: "1.0.0"
                  title: "API"
                  description: "Added description"
                paths: {}            
            """.trimIndent()
        )
    }

    @Test
    fun `overlay should add integer to yaml file`() {
        // Given an yaml document
        val targetDocument = """
            example:
              key: "key"           
            """.trimIndent()

        // And overlay by which we add entry with integer value
        val overlay = """
                {
                    "overlay": "0.1.0",
                    "info": {
                    "title": "Adding integer by overlay",
                    "version": "1.0.0"
                },
                    "actions": [
                        {
                            "target": "example",
                            "update": {
                                "value": 0
                            }
                        }    
                    ]
                }
        """.trim()

        // When applying overlay to target document
        val result = applyOverlay(overlay, targetDocument)

        // Then result of overlay is that description is added
        val returnedYaml = objectMapper.readTree(result)
        assertJson(returnedYaml).isEqualToYaml(
            """
                example:
                  key: "key"
                  value: 0         
            """.trimIndent()
        )
    }

    @Test
    fun `overlay should add nested Nodes to yaml file`() {
        // Given an open API specification without description
        val targetDocument = """
                openapi: "3.0.0"
                info:
                  version: "1.0.0"
                  title: "API"
                paths: {}            
            """.trimIndent()
        // And overlay by which we add description
        val overlay = """
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
        """.trim()

        // When applying overlay to target document
        val result = applyOverlay(overlay, targetDocument)

        // Then result of overlay is that description is added
        val returnedYaml = objectMapper.readTree(result)
        assertJson(returnedYaml).isEqualToYaml(
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
            """.trimIndent()
        )
    }

    @Test
    fun `overlay should add multiple Nodes to json file`() {
        // Given an open API specification without description
        val targetDocument = """
            {
                "openapi": "3.0.0",
                "info": {
                    "version": "1.0.0",
                    "title": "API"
                },
                "paths": {}
            }            
            """.trimIndent()
        // And overlay by which we add description and terms of service
        val overlay = """
                {
                    "overlay": "0.1.0",
                    "info": {
                        "title": "Adding information overlay",
                        "version": "1.0.0"
                    },
                    "actions": [
                        {
                            "target": "info",
                            "description": "Adding extra information to openapi specification",
                            "update": {
                                "description": "Added description", 
                                "termsOfService": "http://example.com/terms/"
                            }
                        }    
                    ]
                }
        """.trim()

        // When applying overlay to target document
        val result = applyOverlay(overlay, targetDocument)

        // Then result of overlay is that description is added
        // val returnedJson = objectMapper.readTree(result)
        assertJson(result).isEqualTo(
            """
            {
                "openapi": "3.0.0",
                "info": {
                    "version": "1.0.0",
                    "title": "API",
                    "description": "Added description",
                    "termsOfService": "http://example.com/terms/"
                },
                "paths": {}
            }
            """.trimIndent()
        )
    }

    @Test
    fun `overlay should add multiple Nodes to yaml file`() {
        // Given an open API specification without description
        val targetDocument = """
                openapi: "3.0.0"
                info:
                  version: "1.0.0"
                  title: "API"
                paths: {}            
            """.trimIndent()
        // And overlay by which we add description and terms of service
        val overlay = """
                {
                    "overlay": "0.1.0",
                    "info": {
                        "title": "Adding information overlay",
                        "version": "1.0.0"
                    },
                    "actions": [
                        {
                            "target": "info",
                            "description": "Adding extra information to openapi specification",
                            "update": {
                                "description": "Added description", 
                                "termsOfService": "http://example.com/terms/"
                            }
                        }    
                    ]
                }
        """.trim()

        // When applying overlay to target document
        val result = applyOverlay(overlay, targetDocument)

        // Then result of overlay is that description is added
        val returnedYaml = objectMapper.readTree(result)
        assertJson(returnedYaml).isEqualToYaml(
            """
                openapi: "3.0.0"
                info:
                  version: "1.0.0"
                  title: "API"
                  description: "Added description"
                  "termsOfService": "http://example.com/terms/"
                paths: {}            
            """.trimIndent()
        )
    }

    @Test
    fun `overlay should apply multiple actions to yaml file`() {
        // Given an open API specification without description
        val targetDocument =  """
                openapi: "3.0.0"
                info:
                  version: "1.0.0"
                  title: "API"
                paths: {}            
            """.trimIndent()
        // And overlay by which we add description and termsOfService
        val overlay = """
                {
                    "overlay": "0.1.0",
                    "info": {
                        "title": "Adding information overlay",
                        "version": "1.0.0"
                    },
                    "actions": [
                        {
                            "target": "info",
                            "description": "Adding description",
                            "update": {
                                "description": "Added description" 
                            }
                        },
                        {                                             
                            "target": "info",
                            "description": "Adding termsOfService",
                            "update": {
                                "termsOfService": "http://example.com/terms/"
                            }
                        }    
                    ]
                }
        """.trim()

        // When applying overlay to target document
        val result = applyOverlay(overlay, targetDocument)

        // Then result of overlay is that description is added
        val returnedYaml = objectMapper.readTree(result)
        assertJson(returnedYaml).isEqualToYaml(
            """
                openapi: "3.0.0"
                info:
                  version: "1.0.0"
                  title: "API"
                  description: "Added description"
                  "termsOfService": "http://example.com/terms/"
                paths: {}            
            """.trimIndent()
        )
    }

    @Test
    fun `overlay should remove object node from document`() {
        // Given open api spec with path /list.
        val targetDocument = """
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
            """.trimIndent()
        // And overlay to remove path list
        val overlay = """
                {
                    "overlay": "0.1.0",
                    "info": {
                        "title": "Adding information overlay",
                        "version": "1.0.0"
                    },
                    "actions": [
                        {
                            "target": "paths.\"/list\"",
                            "remove": true
                        }
                    ]
                }
        """.trim()

        // When applying overlay
        val result = applyOverlay(overlay, targetDocument)

        // Then result of overlay is that path /list removed
        val returnedYaml = objectMapper.readTree(result)
        assertJson(returnedYaml).isEqualToYaml(
            """
                openapi: "3.0.0"
                info:
                  version: "1.0.0"
                  title: "API"
                paths: {}            
            """.trimIndent()
        )
    }

    @Test
    fun `overlay should remove node from document`() {
        // Given an open api spec with description
        val targetDocument = """
                openapi: "3.0.0"
                info:
                  version: "1.0.0"
                  title: "API"
                  description: "Description of API"
                paths: {}            
            """.trimIndent()
        // And overlay to remove description
        val overlay = """
                {
                    "overlay": "0.1.0",
                    "info": {
                        "title": "Adding information overlay",
                        "version": "1.0.0"
                    },
                    "actions": [
                        {
                            "target": "info.description",
                            "remove": true
                        }
                    ]
                }
        """.trim()

        // When applying overlay
        val result = applyOverlay(overlay, targetDocument)

        // Then result of overlay is that description is removed
        val returnedYaml = objectMapper.readTree(result)
        assertJson(returnedYaml).isEqualToYaml(
            """
                openapi: "3.0.0"
                info:
                  version: "1.0.0"
                  title: "API"
                paths: {}            
            """.trimIndent()
        )
    }

    private fun applyOverlay(overlay: String, document: String? = null): String {

        val targetDocument = document?.let {
            val tempFile = createTempFile()
            tempFile.writeText(document)
            tempFile.toAbsolutePath()
        }

        return Overlay.apply(overlay.toStream(), targetDocument)
    }
}
