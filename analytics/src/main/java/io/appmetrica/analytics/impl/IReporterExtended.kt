package io.appmetrica.analytics.impl

import io.appmetrica.analytics.IModuleReporter
import io.appmetrica.analytics.IReporter

interface IReporterExtended : IReporter, IUnhandledSituationReporter, IModuleReporter
