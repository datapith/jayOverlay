package io.datapit.jayOverlay.steps

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.datapit.jayOverlay.Overlay
import io.datapit.jayOverlay.toStream
import io.mockk.InternalPlatformDsl.toStr
import uk.org.webcompere.modelassert.json.JsonAssertions.assertJson
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText

const val FILE_TYPE = "fileType"
const val OVERLAY = "overlay"
const val RESULT = "result"
const val TARGET_DOCUMENT = "targetDocument"

class OverlaySteps : AbstractSteps() {

    private val placeHolder = "<<.*>>".toRegex()

    @Given("a {string} document")
    fun `a target document`(fileType: String, targetDocument: String) {
        setVariable(FILE_TYPE, fileType)
        setVariable(TARGET_DOCUMENT, targetDocument)
    }

    @Given("a overlay")
    fun `a overlay`(overlay: String) {
        // Create temporary file for target document
        val targetDocument = requireNotNull(getVariable<String>(TARGET_DOCUMENT)) { "No target document found"}
        val targetDocumentFile = createTempFile()
        targetDocumentFile.writeText(targetDocument)

        // Replace placeholder extends by location temporary file
        val completeOverlay = overlay.replace(placeHolder, targetDocumentFile.toAbsolutePath().toStr())

        setVariable(OVERLAY, completeOverlay)
    }

    @When("applying overlay to document")
    fun `applying overlay to target document`() {
        val overlay = requireNotNull(getVariable<String>(OVERLAY))
        setVariable(RESULT, Overlay.apply(overlay.toStream()))
    }

    @Then("updates are applied to document")
    fun `updates are applied to document`(expectedDocument: String) {
        val result = requireNotNull(getVariable<String>(RESULT))
        val fileType = requireNotNull(getVariable<String>(FILE_TYPE))

        when(fileType) {
            "YAML" -> {
                val objectMapper = YAMLMapper.builder().build()
                val returnedDocument = objectMapper.readTree(result)
                assertJson(returnedDocument).isEqualToYaml(expectedDocument)
            }
        }
    }
}
