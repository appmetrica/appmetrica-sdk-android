package io.appmetrica.analytics.coreapi.internal.event

interface CounterReportApi {

    var type: Int

    var customType: Int

    var name: String?

    var value: String?

    var valueBytes: ByteArray?

    var bytesTruncated: Int

    var extras: MutableMap<String, ByteArray>
}
