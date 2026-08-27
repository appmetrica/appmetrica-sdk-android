package io.appmetrica.analytics.coreutils.internal.toggle

class TestSimpleThreadSafeToggle(initialValue: Boolean) :
    SimpleThreadSafeToggle(
        initialState = initialValue,
        tag = "Test"
    ) {

    fun notifyState(value: Boolean) {
        updateState(value)
    }
}
