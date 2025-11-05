package io.appmetrica.analytics.impl.client.connection

class ServiceDescription(
    val packageName: String,
    val serviceScheme: String,
    val serviceClass: Class<*>
) {

    override fun toString(): String {
        return "ServiceDescription(" +
            "packageName='$packageName', " +
            "serviceScheme='$serviceScheme', " +
            "serviceClass=$serviceClass" +
            ")"
    }
}
