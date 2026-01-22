package io.appmetrica.analytics.impl.core

internal class CoreImplFirstCreateTaskLauncherProvider {

    fun getLauncher() = CoreImplFirstCreateTaskLauncher(CoreImplFirstCreateTaskProvider().tasks())
}
