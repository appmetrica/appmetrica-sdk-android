package io.appmetrica.analytics.impl

import io.appmetrica.analytics.ModuleEvent.Category

class EventCategoryToSourceConverter {

    fun convert(value: Category): EventSource =
        when (value) {
            Category.GENERAL -> EventSource.NATIVE
            Category.SYSTEM -> EventSource.SYSTEM
        }
}
