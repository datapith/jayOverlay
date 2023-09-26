package io.datapit.jayOverlay

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.burt.jmespath.jackson.JacksonRuntime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import mu.two.KotlinLogging

/**
 * Represents one or more changes to be applied to the target document at the location defined by the target
 * [JSON pointer](https://datatracker.ietf.org/doc/html/rfc6901) expression.
 *
 * @property target A JSON pointer expression referencing the target objects in the target document.
 * @property description A description of the action.
 * @property update An object with the properties and values to be merged with the object(s) referenced by the target.
 *           This property has no impact if remove property is true. The properties of the update object MUST be
 *           compatible with the target object referenced by the JMESPath key.
 * @property remove A boolean value that indicates that the target object is to be removed from the map or array
 *           it is contained in.
 */
@Serializable
data class Action(
    val target: String,
    val description: String? = null,
    val update: Map<String, @Contextual Any>? = null,
    val remove: Boolean = false,
) {
    private val logger = KotlinLogging.logger {}

    init {
        if (!remove) {
            // check update(s) for properties in document are specified
            require(update != null && update.isNotEmpty()) { "No updates for properties in target object found" }
        }
    }

    fun execute(targetDocument: JsonNode): JsonNode {
        // Select target object in document using jmeshpath
        val jmespath = JacksonRuntime()
        val expression = jmespath.compile(target)
        val targetObject = expression.search(targetDocument)

        require(targetObject != null && (targetObject is ObjectNode || jmespath.isTruthy(targetObject))) {
            "Could not find $target"
        }
        if (remove) {
            logger.info { "Removing $targetObject" }
            val parent = findParent(targetDocument, targetObject)
            val removed = parent?.remove(targetObject) ?: throw Exception("Couldn't remove $targetObject from document")
            if (!removed) {
                throw Exception("Couldn't remove $targetObject from $parent")
            }
        } else {
            require(targetObject is ObjectNode) { "Only object nodes can be updated" }
            logger.info { "Applying updates to $targetObject" }
            processUpdates(targetObject, update)
        }

        return targetDocument
    }

    private fun findParent(root: JsonNode, targetObject: JsonNode): ObjectNode? {
        return if (root.has(targetObject)) {
            root as ObjectNode
        } else {
            root.fields().forEach { child ->
                val foundParent = findParent(child.value, targetObject)
                if (foundParent != null)
                    return foundParent
            }
            null
        }
    }

    private fun ObjectNode.remove(targetObject: JsonNode): Boolean {
        fields().forEach {
            if (it.value == targetObject) {
                return this.remove(it.key) != null
            }
        }
        return false
    }

    private fun JsonNode.has(targetObject: JsonNode): Boolean {
        fields().forEach {
            if (it.value == targetObject) return true
        }
        return false
    }

    private fun processUpdates(targetObject: ObjectNode, properties: Map<String, Any>?) {
        properties?.forEach {
            if (targetObject.has(it.key)) {
                logger.info { "Updating ${it.key} in $targetObject to ${it.value}" }
            } else {
                logger.info { "Adding ${it.key} with value ${it.value} to $targetObject" }
            }
            setProperty(targetObject, it.key, it.value)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun setProperty(targetObject: ObjectNode, name: String, value: Any) {
        when (value) {
            is Map<*, *> -> {
                val objectNode = targetObject.putObject(name)
                processUpdates(objectNode, value as Map<String, Any>)
            }
            is List<*> -> {
                val objectNode = targetObject.putArray(name)
                setProperty(objectNode, value)
            }
            is Boolean -> targetObject.put(name, value)
            is String -> targetObject.put(name, value)
            else -> throw UnsupportedOperationException(
                "Detected unsupported type (${value::class}) while setting property $name in $targetObject"
            )
        }
    }

    private fun setProperty(targetObject: ArrayNode, values: List<*>) {
        values.forEach {
            requireNotNull(it) { "Can't add null to array" }
            when (it) {
                // TODO: support Map so that we can also add objects in array
                is String -> targetObject.add(it)
                else -> throw UnsupportedOperationException(
                    "Detected unsupported type (${it::class.java}) while adding value to array"
                )
            }
        }
    }
}
