package io.appmetrica.analytics.coreapi.internal.servicecomponents

data class ServiceModuleCounterReport(
    val name: String?,
    val value: String?,
    val valueBytes: ByteArray?,
    val type: Int,
) {

    companion object {

        fun newBuilder() = Builder()
    }

    class Builder {

        private var name: String? = null
        private var value: String? = null
        private var valueBytes: ByteArray? = null
        private var type: Int = 0

        fun withName(name: String?): Builder = apply {
            this.name = name
        }

        fun withValue(value: String?): Builder = apply {
            this.value = value
        }

        fun withValueBytes(valueBytes: ByteArray?) = apply {
            this.valueBytes = valueBytes
        }

        fun withType(type: Int): Builder = apply {
            this.type = type
        }

        fun build() = ServiceModuleCounterReport(
            name = name,
            value = value,
            valueBytes = valueBytes,
            type = type,
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ServiceModuleCounterReport) return false

        if (type != other.type) return false
        if (name != other.name) return false
        if (value != other.value) return false
        if (!valueBytes.contentEquals(other.valueBytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (value?.hashCode() ?: 0)
        result = 31 * result + (valueBytes?.contentHashCode() ?: 0)
        return result
    }
}
