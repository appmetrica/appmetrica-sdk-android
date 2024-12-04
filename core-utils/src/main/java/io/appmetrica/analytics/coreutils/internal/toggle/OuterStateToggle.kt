package io.appmetrica.analytics.coreutils.internal.toggle

class OuterStateToggle(
    initialState: Boolean,
    tag: String
) : SimpleThreadSafeToggle(initialState, tag) {

    fun update(state: Boolean) {
        super.updateState(state)
    }
}
