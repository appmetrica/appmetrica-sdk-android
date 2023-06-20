package io.appmetrica.analytics.modulesapi.internal.event

import io.appmetrica.analytics.coreapi.internal.event.CounterReportApi

class ModuleCounterReport(
    override var type: Int,
    override var customType: Int,
    override var name: String?,
    override var value: String?,
    override var valueBytes: ByteArray?,
    override var bytesTruncated: Int,
    override var extras: MutableMap<String, ByteArray>
) : CounterReportApi
