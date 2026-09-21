package io.appmetrica.analytics.impl

import org.mockito.ArgumentMatcher

internal class CoreClientEventMatcher private constructor() : ArgumentMatcher<CoreClientEvent> {

    private var eventType: InternalEvents? = null

    fun withType(type: InternalEvents): CoreClientEventMatcher {
        eventType = type
        return this
    }

    override fun matches(argument: CoreClientEvent?): Boolean {
        return argument != null && argument.type == eventType!!.typeId
    }

    companion object {
        @JvmStatic
        fun newMatcher(): CoreClientEventMatcher = CoreClientEventMatcher()
    }
}
