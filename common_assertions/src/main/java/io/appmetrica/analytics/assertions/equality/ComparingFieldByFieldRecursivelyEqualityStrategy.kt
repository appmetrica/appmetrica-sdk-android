package io.appmetrica.analytics.assertions.equality

import org.assertj.core.api.ProxyableObjectAssert

class ComparingFieldByFieldRecursivelyEqualityStrategy : EqualityStrategy {

    override fun isEqual(objectAssert: ProxyableObjectAssert<Any?>, expected: Any?): ProxyableObjectAssert<Any?> {
        return objectAssert.isEqualToComparingFieldByFieldRecursively(expected)
    }
}
