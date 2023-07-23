package io.appmetrica.analytics.ndkcrashesapi.internal

class NativeCrash private constructor(
    val source: NativeCrashSource,
    val handlerVersion: String,
    val uuid: String,
    val dumpFile: String,
    val creationTime: Long,
    val metadata: String,
) {
    // add new fields with default value and by 'with' method to maintain compatibility with ndkcrashes 3.0.0
    class Builder(
        private val source: NativeCrashSource,
        private val handlerVersion: String,
        private val uuid: String,
        private val dumpFile: String,
        private val creationTime: Long,
        private val metadata: String,
    ) {
        fun build() = NativeCrash(
            source,
            handlerVersion,
            uuid,
            dumpFile,
            creationTime,
            metadata,
        )
    }
}
