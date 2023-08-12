package io.appmetrica.analytics.gradle.protobuf

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

abstract class ProtobufExtension @Inject constructor(
    private val objectFactory: ObjectFactory
) {
    abstract val packageName: Property<String>
    abstract val protoPath: Property<String>
    abstract val protoConfigs: ListProperty<ProtoConfig>

    fun protoFile(params: Map<String, Any?>) {
        protoFile(
            srcPath = params.getValue("srcPath").toString(),
            years = params["years"]?.toString()
        )
    }

    fun protoFile(
        srcPath: String,
        years: String? = null
    ) {
        val config = objectFactory.newInstance<ProtoConfig>()
        config.srcPath.set(srcPath)
        config.years.set(years)
        protoConfigs.add(config)
    }
}
