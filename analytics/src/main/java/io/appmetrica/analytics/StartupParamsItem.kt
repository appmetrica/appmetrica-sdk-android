package io.appmetrica.analytics

class StartupParamsItem(
    val id: String?,
    val status: StartupParamsItemStatus,
    val errorDetails: String?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StartupParamsItem

        if (id != other.id) return false
        if (status != other.status) return false
        if (errorDetails != other.errorDetails) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + status.hashCode()
        result = 31 * result + (errorDetails?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "StartupParamsItem(id=$id, status=$status, errorDetails=$errorDetails)"
    }
}
