package io.datapit.jayOverlay.steps

import kotlin.reflect.KClass

abstract class AbstractSteps {

    companion object {
        private val storage = mutableMapOf<Any, ValueHolder<*>>()
    }

    protected fun setVariable(id: Any, value: Any?) {
        storage.put(id, ValueHolder(value))
    }

    protected fun <T> getVariable(id: Any): T? {
        val valueHolder = storage[id]
        if (valueHolder == null) {
            throw Exception("Variable $id not found in test context")
        } else {
            return if (valueHolder.value == null) null else valueHolder.value as T?
                ?: throw ClassCastException("Cannot cast ${valueHolder.type} to specified type")
        }
    }

    /**
     * Remove variable from the map
     *
     * @param id the key to remove
     * @return the previous value associated with the key, or null if the key was not present in the map.
     */
    protected fun <T> removeVariable(id: Any): T? {
        val valueHolder = storage.remove(id)
        return if (valueHolder == null) {
            null
        } else {
            if (valueHolder.value == null) null else valueHolder.value as T?
                ?: throw ClassCastException("Cannot cast ${valueHolder.type} to specified type")
        }
    }

    fun resetContext() {
        storage.clear()
    }

    private data class ValueHolder<T>(val value: T?) {

        // Type of value in storage
        val type: KClass<*> = if (value == null) Any::class else value!!::class
    }
}
