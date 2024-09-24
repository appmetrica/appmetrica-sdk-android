package io.appmetrica.analytics.impl

import io.appmetrica.analytics.IModuleReporter
import io.appmetrica.analytics.IReporter
import io.appmetrica.analytics.impl.crash.jvm.client.AnrReporter

interface IReporterExtended : IReporter, IUnhandledSituationReporter, IModuleReporter, AnrReporter
