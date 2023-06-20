package io.appmetrica.analytics.assertions.equality

import org.assertj.core.api.ProxyableObjectAssert

class ComparingFieldByFieldEqualityStrategy : EqualityStrategy {

    override fun isEqual(objectAssert: ProxyableObjectAssert<Any?>, expected: Any?): ProxyableObjectAssert<Any?> {
        return objectAssert.isEqualToComparingFieldByField(expected)
    }
}
