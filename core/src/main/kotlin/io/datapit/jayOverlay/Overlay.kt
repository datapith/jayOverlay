package io.datapit.jayOverlay

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.exc.StreamReadException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.ByteArrayInputStream
import java.io.InputStream
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import mu.two.KotlinLogging
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.Charset
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import org.apache.commons.lang3.exception.ExceptionUtils

/**
 * This is the root object of the Overlay json document.
 *
 * @property overlay The version number of the Overlay Specification that the Overlay document uses.
 * @property info Metadata about the Overlay
 * @property extends URL or (relative) file path to the target document this overlay applies to
 * @property actions An ordered list of actions to be applied to the target document. The list MUST contain at least
 *           one value.
 */
@Serializable
data class Overlay(
    val overlay: String,
    val info: Info,
    val extends: String?,
    val actions: List<Action>,
) {
    private val logger = KotlinLogging.logger {}

    @Transient
    private val yamlMapper = YAMLMapper.builder()
        // By default, the modified yaml file will start with tree dashes. That’s perfectly valid for the YAML format,
        // but we can turn it off by disabling the feature on the YAMLFactory:
        .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
        .build()

    @Transient
    private val jsonMapper = JsonMapper.builder().build()

    companion object {
        private val logger = KotlinLogging.logger {}

        private val objectMapper =
            ObjectMapper().registerKotlinModule().setSerializationInclusion(JsonInclude.Include.NON_NULL)

        fun apply(overlayDocument: Path, targetDocument: Path?=null): String =
            apply(overlayDocument.inputStream(), targetDocument)

        fun apply(overlayDocument: InputStream, targetDocument: Path? = null): String {
            logger.info { "Starting to apply overlay." }
            val modifiedDocument = try {
                val overlay = objectMapper.readValue<Overlay>(overlayDocument)
                overlay.execute(targetDocument)
            } catch (exception: Exception) {
                throw ExceptionUtils.getRootCause(exception)
            }
            logger.info { "Result of overlay is:\n$modifiedDocument" }
            return modifiedDocument
        }

    }

    init {
        require(actions.isNotEmpty()) { "There must be at least one action within the overlay" }
    }

    /**
     * Applies overlay to document. If document is null the overlay will be applied to the file that is specified by
     * extends.
     *
     * @param document optional document at which overlay will be applied. If null the overlay wi;;
     */
    private fun execute(document: Path? = null): String {
        // Use target document in case specified, otherwise use document of overlay file (extends)
        val pathDocument = document ?: run {
            require(extends != null && extends.isNotEmpty()) {"extends is missing"}
            try {
                Paths.get(URL(extends).toURI())
            } catch (error: MalformedURLException) {
                // Create path to local file
                Paths.get(extends)
            }
        }
        require(pathDocument.exists()) { "Document $pathDocument does not exist" }

        logger.info { "load target document from $extends" }
        val (mapper, targetDocument) = parseTargetDocument(pathDocument)

        logger.info { "apply actions to target document" }
        actions.forEach {
            it.execute(targetDocument)
        }

        return mapper.writeValueAsString(targetDocument)
    }

    /**
     * Parses yaml/json document and returns object mapper by which the document was parsed and the parsed document
     */
    private fun parseTargetDocument(targetDocumentLocation: Path): Pair<ObjectMapper, JsonNode> {
        return try {
            // try to read target documnt as JSON
            targetDocumentLocation.inputStream().use { jsonMapper to jsonMapper.readTree(it) }
        } catch (exception: StreamReadException) {
            try {
                // Try to read target document as YAML
                targetDocumentLocation.inputStream().use { yamlMapper to yamlMapper.readTree(it) }
            } catch (exception: StreamReadException) {
                throw Exception("target document $targetDocumentLocation in not a valid json or yaml file")
            }
        }
    }
}

fun String.toStream(charset: Charset = Charsets.UTF_8) = ByteArrayInputStream(toByteArray(charset))
