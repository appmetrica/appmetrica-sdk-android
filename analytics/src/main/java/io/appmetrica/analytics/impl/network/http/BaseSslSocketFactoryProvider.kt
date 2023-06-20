package io.appmetrica.analytics.impl.network.http

import io.appmetrica.analytics.coreapi.internal.io.SslSocketFactoryProvider
import io.appmetrica.analytics.impl.StartupStateObserver

interface BaseSslSocketFactoryProvider : SslSocketFactoryProvider, StartupStateObserver
