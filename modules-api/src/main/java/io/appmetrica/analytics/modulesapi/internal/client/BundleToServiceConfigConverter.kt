package io.appmetrica.analytics.modulesapi.internal.client

import android.os.Bundle

interface BundleToServiceConfigConverter<T> {

    fun fromBundle(bundle: Bundle): T
}
