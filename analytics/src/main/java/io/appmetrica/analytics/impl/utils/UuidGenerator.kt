package io.appmetrica.analytics.impl.utils

import java.util.Locale
import java.util.UUID

internal class UuidGenerator {

    fun generateUuid(): String = UUID.randomUUID().toString().replace("-", "").toLowerCase(Locale.US)
}
