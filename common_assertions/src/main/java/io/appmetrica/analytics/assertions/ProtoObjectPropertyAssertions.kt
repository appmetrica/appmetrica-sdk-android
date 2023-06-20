package io.appmetrica.analytics.assertions

class ProtoObjectPropertyAssertions<T : Any>(actual: T?) : JavaObjectPropertyAssertions<T>(actual) {

    init {
        withFinalFieldOnly(false)
    }

    override fun <S : Any> createNestedAssertions(actual: S?): ObjectPropertyAssertions<S> {
        return ProtoObjectPropertyAssertions(actual)
    }
}
