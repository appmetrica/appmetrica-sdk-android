package io.appmetrica.analytics.coreapi.internal.event

interface ClientEvent {

    var type: Int

    var customType: Int

    var name: String?

    var value: String?

    var valueBytes: ByteArray?

    var valueProtocolVersion: Int?

    var bytesTruncated: Int

    var extras: MutableMap<String, ByteArray>

    var profileID: String?

    var eventEnvironment: String?
}
