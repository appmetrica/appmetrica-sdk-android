package io.appmetrica.analytics.coreutils.internal

class ReferenceHolder {

    private val references: MutableSet<Any> = mutableSetOf()

    fun storeReference(reference: Any) {
        references.add(reference)
    }

    fun removeReference(reference: Any) {
        references.remove(reference)
    }

    fun peekReferences(): Set<Any> {
        return references.toSet()
    }
}
