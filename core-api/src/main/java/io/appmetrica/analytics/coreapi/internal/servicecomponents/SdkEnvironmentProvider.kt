package io.appmetrica.analytics.coreapi.internal.servicecomponents

import io.appmetrica.analytics.coreapi.internal.model.SdkEnvironment

interface SdkEnvironmentProvider {

    val sdkEnvironment: SdkEnvironment
}
