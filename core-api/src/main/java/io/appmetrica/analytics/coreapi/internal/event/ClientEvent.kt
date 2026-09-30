package io.appmetrica.analytics.coreapi.internal.event

interface ClientEvent {

    val type: Int

    val customType: Int

    val name: String?

    val valueBytes: ByteArray?

    val valueProtocolVersion: Int?

    val bytesTruncated: Int

    val extras: Map<String, ByteArray>

    val profileID: String?

    val eventEnvironment: String?
}
