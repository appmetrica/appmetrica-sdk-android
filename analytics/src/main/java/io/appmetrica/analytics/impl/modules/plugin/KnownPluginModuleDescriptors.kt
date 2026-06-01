package io.appmetrica.analytics.impl.modules.plugin

internal object KnownPluginModuleDescriptors {

    val ALL = listOf(
        PluginModuleDescriptor(
            moduleName = "plugin:Flutter:AppMetrica",
            detectionStrategy = PluginDetectionStrategies.byClass(
                "io.appmetrica.analytics.flutter.AppMetricaPlugin"
            )
        ),
        PluginModuleDescriptor(
            moduleName = "plugin:React:AppMetrica",
            detectionStrategy = PluginDetectionStrategies.byClass(
                "io.appmetrica.analytics.reactnative.AppMetricaPackage"
            )
        ),
        PluginModuleDescriptor(
            moduleName = "plugin:React:Varioqub",
            detectionStrategy = PluginDetectionStrategies.byClass(
                "com.varioqub.reactnative.VarioqubPackage"
            )
        ),
        PluginModuleDescriptor(
            moduleName = "plugin:Unity:AdRevenueAdapter",
            detectionStrategy = PluginDetectionStrategies.byClass(
                "io.appmetrica.adrevenueadapter.plugin.unity.AdRevenueAdapterProxy"
            )
        ),
        PluginModuleDescriptor(
            moduleName = "plugin:Unity:AppMetrica",
            detectionStrategy = PluginDetectionStrategies.byClass(
                "io.appmetrica.analytics.plugin.unity.AppMetricaProxy"
            )
        ),
    )
}
