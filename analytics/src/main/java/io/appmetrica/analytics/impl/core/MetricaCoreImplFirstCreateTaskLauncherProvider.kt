package io.appmetrica.analytics.impl.core

class MetricaCoreImplFirstCreateTaskLauncherProvider {

    fun getLauncher() = MetricaCoreImplFirstCreateTaskLauncher(MetricaCoreImplFirstCreateTaskProvider().tasks())
}
