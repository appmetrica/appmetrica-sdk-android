package io.appmetrica.analytics.impl.location

import io.appmetrica.analytics.assertions.JavaObjectPropertyAssertions
import java.lang.reflect.Field

/**
 * This subclass is used for modifying reflective call package for extraction object properties
 * with package private access
 */
internal class PackagePrivateObjectPropertyAssertions<T : Any>(actual: T) : JavaObjectPropertyAssertions<T>(actual) {

    public override fun <F> getFieldValueReflective(field: Field): F {
        return field.get(actual) as F
    }
}
