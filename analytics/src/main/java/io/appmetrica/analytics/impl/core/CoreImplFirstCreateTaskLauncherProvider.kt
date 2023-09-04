package io.appmetrica.analytics.impl.core

class CoreImplFirstCreateTaskLauncherProvider {

    fun getLauncher() = CoreImplFirstCreateTaskLauncher(CoreImplFirstCreateTaskProvider().tasks())
}
