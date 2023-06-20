package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.modulesapi.internal.LocationExtension
import io.appmetrica.analytics.modulesapi.internal.ModuleEntryPoint
import io.appmetrica.analytics.modulesapi.internal.ModuleRemoteConfig
import io.appmetrica.analytics.modulesapi.internal.ModuleServicesDatabase
import io.appmetrica.analytics.modulesapi.internal.RemoteConfigExtensionConfiguration
import io.appmetrica.analytics.modulesapi.internal.ServiceContext
import io.appmetrica.analytics.modulesapi.internal.event.ModuleEventHandlerFactory
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ModuleLoaderTest : CommonTest() {

    private val moduleLoader = ModuleLoader()

    @Test
    fun loadModule() {
        assertThat(moduleLoader.loadModule(TestClass1::class.java.name))
            .isNotNull
    }

    @Test
    fun loadWrongModule() {
        assertThat(moduleLoader.loadModule(TestClass2::class.java.name))
            .isNull()
    }

    @Test
    fun loadModuleWithoutDefaultConstructor() {
        assertThat(moduleLoader.loadModule(TestClass3::class.java.name))
            .isNull()
    }

    @Test
    fun loadModuleIfEntryPointClassNotPresent() {
        assertThat(moduleLoader.loadModule("io.appmetrica.analytics.impl.modules.TestClassNotPresent"))
            .isNull()
    }

}

class TestClass1 : ModuleEntryPoint<Any> {
    override val identifier: String
        get() = "Some string"

    override val remoteConfigExtensionConfiguration: RemoteConfigExtensionConfiguration<Any>?
        get() = null

    override val moduleEventHandlerFactory: ModuleEventHandlerFactory? = null

    override fun initServiceSide(serviceContext: ServiceContext, config: ModuleRemoteConfig<Any?>) {
    }

    override val locationExtension: LocationExtension?
        get() = null

    override val moduleServicesDatabase: ModuleServicesDatabase?
        get() = null
}

class TestClass2 : ModuleEntryPoint<Any> {
    constructor(arg1: Any)

    override val identifier: String
        get() = "some identifier"

    override val remoteConfigExtensionConfiguration: RemoteConfigExtensionConfiguration<Any>?
        get() = null

    override val moduleEventHandlerFactory: ModuleEventHandlerFactory? = null

    override fun initServiceSide(serviceContext: ServiceContext, config: ModuleRemoteConfig<Any?>) {
    }

    override val locationExtension: LocationExtension?
        get() = null

    override val moduleServicesDatabase: ModuleServicesDatabase?
        get() = null
}

class TestClass3
