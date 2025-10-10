package io.appmetrica.analytics.idsync.internal.model

class Preconditions(
    val networkType: NetworkType
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Preconditions

        return networkType == other.networkType
    }

    override fun hashCode(): Int {
        return networkType.hashCode()
    }

    override fun toString(): String {
        return "Preconditions(networkType=$networkType)"
    }
}
