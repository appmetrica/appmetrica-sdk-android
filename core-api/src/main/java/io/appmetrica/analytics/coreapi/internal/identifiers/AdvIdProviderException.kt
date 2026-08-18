package io.appmetrica.analytics.coreapi.internal.identifiers

abstract class AdvIdProviderException(
    message: String?,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

class AdvIdServiceNotFoundException(
    message: String?,
    cause: Throwable? = null,
) : AdvIdProviderException(message, cause) {
    constructor(message: String?) : this(message, null)
}

class AdvIdServiceBindingException(
    message: String?,
    cause: Throwable? = null,
) : AdvIdProviderException(message, cause) {
    constructor(message: String?) : this(message, null)
}

class AdvIdServiceConnectionTimeoutException(
    message: String?,
    cause: Throwable? = null,
) : AdvIdProviderException(message, cause) {
    constructor(message: String?) : this(message, null)
}

class AdvIdServiceCommunicationException(
    message: String?,
    cause: Throwable? = null,
) : AdvIdProviderException(message, cause) {
    constructor(message: String?) : this(message, null)
}

class AdvIdServiceResponseException(
    message: String?,
    cause: Throwable? = null,
) : AdvIdProviderException(message, cause) {
    constructor(message: String?) : this(message, null)
}

class AdvIdServiceAccessDeniedException(
    message: String?,
    cause: Throwable? = null,
) : AdvIdProviderException(message, cause) {
    constructor(message: String?) : this(message, null)
}
