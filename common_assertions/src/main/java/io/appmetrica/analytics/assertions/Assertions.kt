package io.appmetrica.analytics.assertions

@Suppress("FunctionName")
fun <T : Any> ObjectPropertyAssertions(actual: T?): ObjectPropertyAssertions<T> =
    when {
        actual == null -> JavaObjectPropertyAssertions(actual)
        actual::class.java.getAnnotation(Metadata::class.java) != null -> KotlinObjectPropertyAssertions(actual)
        else -> JavaObjectPropertyAssertions(actual)
    }
