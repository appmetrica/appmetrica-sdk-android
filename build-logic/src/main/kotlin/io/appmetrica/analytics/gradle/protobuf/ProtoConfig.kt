package io.appmetrica.analytics.gradle.protobuf

import org.gradle.api.provider.Property

abstract class ProtoConfig {
    abstract val srcPath: Property<String>
    abstract val years: Property<String>
}
