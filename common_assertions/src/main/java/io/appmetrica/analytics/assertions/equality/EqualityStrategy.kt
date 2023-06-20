package io.appmetrica.analytics.assertions.equality

import org.assertj.core.api.ProxyableObjectAssert

interface EqualityStrategy {

    fun isEqual(objectAssert: ProxyableObjectAssert<Any?>, expected: Any?): ProxyableObjectAssert<Any?>
}
